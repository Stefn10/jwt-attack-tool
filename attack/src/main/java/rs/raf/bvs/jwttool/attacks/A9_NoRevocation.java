package rs.raf.bvs.jwttool.attacks;

import org.json.JSONObject;
import rs.raf.bvs.jwttool.http.ApiClient;
import rs.raf.bvs.jwttool.jwt.JwtCodec;
import rs.raf.bvs.jwttool.jwt.JwtParts;

// a stateless JWT with no revocation list and no exp cannot be remotely invalidated
// a leaked token works until exp, and since the target never sets exp (A5) it works forever
// Test in 4 steps:
//   1. original token works (baseline 200)
//   2. try a logout endpoint (probably 404)
//   3. after logout the token still works -> proof of no-revocation
//   4. a password change does not invalidate the old token
public final class A9_NoRevocation implements Attack {

    // possible logout endpoints, we try them all
    private static final String[] LOGOUT_PATHS = {
            "/auth/logout",
            "/logout",
            "/users/logout",
            "/users/me/logout"
    };

    @Override
    public AttackResult run(AttackContext ctx) throws Exception {
        AttackResult r = new AttackResult("Attack 9 - No-Revocation / Logout Test");

        if (ctx.getToken() == null) {
            r.fail("No token. Log in first.");
            return r;
        }

        String testPath;
        if (ctx.getTestEndpoint() != null) {
            testPath = ctx.getTestEndpoint();
        } else {
            testPath = "/users";
        }

        ApiClient api = ctx.api();
        String token = ctx.getToken();

        r.section("Step 1 - Baseline: original token works");
        ApiClient.HttpResult baseline = api.get(testPath, token);
        r.info("GET " + testPath + " -> HTTP " + baseline.status);
        if (baseline.status == 200) {
            r.ok("Token valid, server returns 200. Baseline OK.");
        } else if (baseline.status == 401) {
            r.fail("Token already invalid (401). Maybe exp? Log in again and run once more.");
            return r;
        } else {
            r.warn("Unexpected baseline status " + baseline.status + " - continuing anyway.");
        }

        r.section("Step 2 - Try a 'logout' endpoint");
        r.info("Sends POST with the Authorization header to common logout paths.");
        boolean anyLogoutAccepted = false;
        for (int i = 0; i < LOGOUT_PATHS.length; i++) {
            String p = LOGOUT_PATHS[i];
            // empty JSON body, logout usually needs no payload
            ApiClient.HttpResult res = api.post(p, "{}", token);
            String marker;
            if (res.status == 404) {
                marker = "[does not exist]";
            } else if (res.status >= 200 && res.status < 300) {
                marker = "[ACCEPTED]";
                anyLogoutAccepted = true;
            } else {
                marker = "[other]";
            }
            r.info("POST " + p + " -> HTTP " + res.status + "  " + marker);
        }
        if (!anyLogoutAccepted) {
            r.warn("No logout endpoint exists - with a stateless JWT logout is client-only (just deleting from localStorage).");
        }

        r.section("Step 3 - Re-test the original token after 'logout'");
        ApiClient.HttpResult afterLogout = api.get(testPath, token);
        r.info("GET " + testPath + " -> HTTP " + afterLogout.status);
        if (afterLogout.status == 200) {
            r.warn("Token STILL works after the logout attempt - NOT revoked.");
            r.warn("Consequence: if the token leaks (XSS, log, screenshot), the attacker uses it until exp.");
            r.warn("And since the target never sets exp (see A5), 'until exp' means FOREVER.");
        } else {
            r.ok("Token no longer works (" + afterLogout.status + ") - revocation appears to exist.");
        }

        r.section("Step 4 - Test: does a password change invalidate the old token?");
        // extract userId from the token (sub or userId) to guess /users/{id}
        JwtParts parts = JwtCodec.split(token);
        String userId = parts.payload.optString("userId",
                          parts.payload.optString("sub", null));
        if (userId == null || userId.isEmpty()) {
            r.info("Token has no userId/sub claim - skipping this step.");
        } else {
            r.info("userId from the token: " + userId);
            JSONObject changePw = new JSONObject()
                    .put("password", "NewPassword123!")
                    .put("confirmPassword", "NewPassword123!");
            ApiClient.HttpResult pwRes = api.post("/users/" + userId + "/password", changePw.toString(), token);
            r.info("POST /users/" + userId + "/password -> HTTP " + pwRes.status);

            if (pwRes.status >= 200 && pwRes.status < 300) {
                r.warn("Password changed. Now testing whether the old token still works.");
                ApiClient.HttpResult afterPwChange = api.get(testPath, token);
                r.info("GET " + testPath + " with the OLD token -> HTTP " + afterPwChange.status);
                if (afterPwChange.status == 200) {
                    r.warn("CLASSIC: the old token works even after a password change.");
                    r.warn("In stateful sessions this would force a logout. A JWT without jti+blacklist cannot.");
                }
            } else {
                r.info("The password-change endpoint does not exist or refuses - skipping the proof, but the point stands.");
            }
        }

        r.setConclusion("A stateless JWT without (a) a short exp, (b) a jti claim + a server-side blacklist = no remote logout. A leaked token = a persistent risk. Fix: (1) short exp (15 min) + refresh tokens, or (2) a jti claim + a Redis SET 'jwt:blacklist' that revoked jti values are added to and that is checked on every verify call.");
        // the vulnerability (no logout possible) exists, so success = false
        r.setSuccess(false);
        return r;
    }
}
