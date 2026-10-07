package rs.raf.bvs.jwttool.attacks;

import rs.raf.bvs.jwttool.http.ApiClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

// passive audit of the transport/storage surface for the JWT
// Crypto is only half the story: if CORS lets any origin through with credentials, or the
// token sits in localStorage with no CSP, the attacker exfiltrates it via XSS/CSRF
// We check:
//   - Access-Control-Allow-Origin: '*' or a reflected evil origin
//   - Allow-Credentials 'true' with '*' (a forbidden combination)
//   - Strict-Transport-Security missing -> HTTPS downgrade
//   - X-Frame-Options / CSP missing -> clickjacking
//   - Set-Cookie: whether it has HttpOnly+Secure+SameSite
public final class A10_TokenSurfaceAudit implements Attack {

    private static final String EVIL_ORIGIN = "http://evil.example.com";

    @Override
    public AttackResult run(AttackContext ctx) throws Exception {
        AttackResult r = new AttackResult("Attack 10 - Token Surface Audit (passive)");

        String path;
        if (ctx.getTestEndpoint() != null) {
            path = ctx.getTestEndpoint();
        } else {
            path = "/users";
        }

        ApiClient api = ctx.api();
        int issues = 0;

        r.section("Step 1 - CORS preflight test with an evil origin");
        r.info("OPTIONS " + path + " with Origin: " + EVIL_ORIGIN);
        ApiClient.HttpResult preflight = api.optionsPreflight(path, EVIL_ORIGIN, "GET");
        r.info("Status: " + preflight.status);

        String allowOrigin = preflight.header("Access-Control-Allow-Origin");
        String allowCreds = preflight.header("Access-Control-Allow-Credentials");
        String allowMethods = preflight.header("Access-Control-Allow-Methods");
        String allowHeaders = preflight.header("Access-Control-Allow-Headers");

        r.info("Access-Control-Allow-Origin: " + nullSafe(allowOrigin));
        r.info("Access-Control-Allow-Credentials: " + nullSafe(allowCreds));
        r.info("Access-Control-Allow-Methods: " + nullSafe(allowMethods));
        r.info("Access-Control-Allow-Headers: " + nullSafe(allowHeaders));

        if (allowOrigin == null) {
            r.ok("Allow-Origin is not set -> the browser CORS check will not let the evil origin through.");
        } else if ("*".equals(allowOrigin)) {
            r.warn("Allow-Origin = '*' -> any site can hit the API from the browser.");
            issues++;
            if (allowCreds != null && allowCreds.equalsIgnoreCase("true")) {
                r.warn("CRITICAL: Allow-Origin '*' + Allow-Credentials 'true' = a disallowed combination (the RFC forbids it, but the server is probably hand-rolled).");
                issues++;
            }
        } else if (allowOrigin.equalsIgnoreCase(EVIL_ORIGIN)) {
            r.warn("CRITICAL: Allow-Origin REFLECTED back as " + EVIL_ORIGIN + " -> no whitelist.");
            r.warn("The attacker can make authenticated requests from his own site.");
            issues++;
        } else {
            r.ok("Allow-Origin is a concrete host (" + allowOrigin + ") - probably a whitelist.");
        }

        r.section("Step 2 - Security headers (HSTS, X-Frame-Options, CSP)");
        // we read the headers from GET, they are often absent in the OPTIONS response
        ApiClient.HttpResult getRes = api.get(path, ctx.getToken());
        Map<String, List<String>> respHeaders = getRes.headers;
        r.info("Inspecting headers from GET " + path + " (HTTP " + getRes.status + ")");

        String hsts = getRes.header("Strict-Transport-Security");
        if (hsts == null || hsts.isEmpty()) {
            r.warn("Strict-Transport-Security is NOT set -> the browser will try an HTTP fallback. The token can be captured in the first plain HTTP request.");
            issues++;
        } else {
            r.ok("Strict-Transport-Security: " + hsts);
        }

        String xfo = getRes.header("X-Frame-Options");
        String csp = getRes.header("Content-Security-Policy");
        if ((xfo == null || xfo.isEmpty()) && (csp == null || csp.isEmpty())) {
            r.warn("Neither X-Frame-Options nor CSP frame-ancestors is set -> the page can be loaded in an iframe -> clickjacking possible.");
            issues++;
        } else {
            if (xfo != null) r.ok("X-Frame-Options: " + xfo);
            if (csp != null) r.ok("Content-Security-Policy set (" + truncate(csp, 80) + ")");
        }

        String xcto = getRes.header("X-Content-Type-Options");
        if (xcto == null || !"nosniff".equalsIgnoreCase(xcto)) {
            r.warn("X-Content-Type-Options is not 'nosniff' -> the browser may sniff the content-type and execute a payload as a script.");
            issues++;
        } else {
            r.ok("X-Content-Type-Options: nosniff");
        }

        r.section("Step 3 - Set-Cookie analysis (if cookie auth is used)");
        // the login endpoint is the best target, that is usually where a session is set
        ApiClient.HttpResult loginProbe = api.post("/users/login", "{\"email\":\"probe@bvs.local\",\"password\":\"probe\"}", null);
        r.info("POST /users/login (probe) -> HTTP " + loginProbe.status);
        List<String> setCookies = findCookieHeaders(loginProbe.headers);
        if (setCookies.isEmpty()) {
            r.info("No Set-Cookie header -> the server uses a pure-JWT-in-Authorization model.");
            r.info("Consequence: the token travels through JS (localStorage/sessionStorage) -> XSS = token exfiltration.");
            r.warn("Best practice for sensitive apps: store the token in an HttpOnly+Secure+SameSite=Strict cookie.");
            issues++;
        } else {
            for (int i = 0; i < setCookies.size(); i++) {
                String c = setCookies.get(i);
                r.info("Set-Cookie: " + truncate(c, 120));
                if (!c.toLowerCase().contains("httponly")) {
                    r.warn("Cookie WITHOUT the HttpOnly flag -> JS can read it (XSS exfil).");
                    issues++;
                }
                if (!c.toLowerCase().contains("secure")) {
                    r.warn("Cookie WITHOUT the Secure flag -> it travels over HTTP.");
                    issues++;
                }
                if (!c.toLowerCase().contains("samesite")) {
                    r.warn("Cookie WITHOUT a SameSite attribute -> CSRF risk.");
                    issues++;
                }
            }
        }

        r.section("Step 4 - Summary conclusion");
        if (issues == 0) {
            r.ok("All checks passed - the transport surface is solidly configured.");
            r.setSuccess(true);
        } else {
            r.warn("Found " + issues + " issue(s) on the transport/storage surface.");
            r.setSuccess(false);
        }

        r.setConclusion("The JWT crypto alone is not enough. The token travels through CORS, is stored in the browser, and depends on HTTPS. Checklist: (1) an Access-Control-Allow-Origin whitelist of concrete origins (never '*' with credentials), (2) a Strict-Transport-Security header with max-age >= 31536000, (3) a Content-Security-Policy with script-src 'self' (prevents XSS-driven exfiltration), (4) if possible, move the token out of localStorage into an HttpOnly+Secure+SameSite=Strict cookie.");
        return r;
    }

    // find all Set-Cookie headers in the map, there can be several
    private static List<String> findCookieHeaders(Map<String, List<String>> headers) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                if (e.getKey() != null && e.getKey().equalsIgnoreCase("Set-Cookie")) {
                    return e.getValue();
                }
            }
        }
        return Collections.<String>emptyList();
    }

    private static String nullSafe(String s) {
        if (s == null) return "(not set)";
        if (s.isEmpty()) return "(empty)";
        return s;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 3) + "...";
    }
}
