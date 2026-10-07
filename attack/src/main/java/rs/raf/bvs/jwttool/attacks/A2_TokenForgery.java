package rs.raf.bvs.jwttool.attacks;

import org.json.JSONObject;
import rs.raf.bvs.jwttool.http.ApiClient;
import rs.raf.bvs.jwttool.jwt.JwtCodec;
import rs.raf.bvs.jwttool.jwt.JwtParts;

// we change role to ADMIN but keep the old signature
// on verify() the server recomputes the HMAC and sees the mismatch -> 401, the attack is expected to fail
public final class A2_TokenForgery implements Attack {

    @Override
    public AttackResult run(AttackContext ctx) throws Exception {
        AttackResult r = new AttackResult("Attack 2 - Token Forgery (tamper, keep old signature)");

        if (ctx.getToken() == null) {
            r.fail("No token. Log in first.");
            return r;
        }

        JwtParts orig = JwtCodec.split(ctx.getToken());
        String currentRole = orig.payload.optString("role", "<none>");

        // if the frontend does not send a role, default to ADMIN
        String newRole;
        if (ctx.getNewRole() != null && !ctx.getNewRole().isEmpty()) {
            newRole = ctx.getNewRole();
        } else {
            newRole = "ADMIN";
        }

        r.info("Current role: " + currentRole);
        r.info("Changing role to: " + newRole);

        // copy of the payload, we change only role
        JSONObject tampered = new JSONObject(orig.payload.toString());
        tampered.put("role", newRole);

        String newPayloadB64 = JwtCodec.b64UrlEncode(tampered.toString());

        // we reuse the old signature even though the payload changed
        // without the secret we cannot build a new valid one
        String forged = JwtCodec.assemble(orig.headerB64, newPayloadB64, orig.signatureB64);

        r.code("Forged token (with old signature)", forged);

        String path;
        if (ctx.getTestEndpoint() != null) {
            path = ctx.getTestEndpoint();
        } else {
            path = "/users";
        }
        ApiClient.HttpResult res = ctx.api().get(path, forged);

        r.info("GET " + path + " -> HTTP " + res.status);
        r.code("Response body", res.body);

        if (res.status == 401) {
            r.ok("Server rejected the token -> SIGNATURE CHECK WORKS.");
            r.setConclusion("com.auth0:java-jwt verify() detected the tampering. This control prevents direct payload edits without the key.");
            r.setSuccess(true);
        } else if (res.status == 200) {
            r.warn("Server accepted the forged token - CRITICAL VULNERABILITY.");
            r.setConclusion("Signature check does not work - the server accepts a modified payload.");
            r.setSuccess(false);
        } else {
            r.warn("Unexpected status " + res.status);
        }

        return r;
    }
}
