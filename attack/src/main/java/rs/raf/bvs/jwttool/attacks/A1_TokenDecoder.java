package rs.raf.bvs.jwttool.attacks;

import org.json.JSONObject;
import rs.raf.bvs.jwttool.jwt.JwtCodec;
import rs.raf.bvs.jwttool.jwt.JwtParts;

// JWT payload is only base64, not encrypted
// anyone holding the token can read every claim
public final class A1_TokenDecoder implements Attack {

    @Override
    public AttackResult run(AttackContext ctx) {
        AttackResult r = new AttackResult("Attack 1 - Token Decoder");

        if (ctx.getToken() == null || ctx.getToken().isEmpty()) {
            r.fail("No token. Log in first (Capture Token).");
            return r;
        }

        // split into header, payload, signature
        JwtParts parts = JwtCodec.split(ctx.getToken());

        r.section("Raw token parts (base64url)");
        r.code("Header",    parts.headerB64);
        r.code("Payload",   parts.payloadB64);
        r.code("Signature", parts.signatureB64 + "  (" + parts.signatureBytes().length + " bytes)");

        r.section("Decoded JSON");
        r.code("HEADER", parts.header.toString(2));
        r.code("PAYLOAD", parts.payload.toString(2));

        r.section("Security-relevant claims");
        flagClaims(r, parts);

        r.warn("PAYLOAD IS NOT ENCRYPTED - anyone with the token reads every claim.");
        r.setConclusion("The token is only base64-encoded. To be secret it must be encrypted (JWE) or carry only non-sensitive data.");
        r.setSuccess(true);
        return r;
    }

    private void flagClaims(AttackResult r, JwtParts parts) {
        JSONObject h = parts.header;
        JSONObject p = parts.payload;

        // alg from the header, the rest from the payload
        // getClaim returns null if the claim is absent
        check(r, "alg",    getClaim(h, "alg"),    "if none/None -> vulnerable (Attack 4)");
        check(r, "role",   getClaim(p, "role"),   "changing it to ADMIN is a typical attack (Attack 2/6)");
        check(r, "sub",    getClaim(p, "sub"),    "identity - target for impersonation");
        check(r, "userId", getClaim(p, "userId"), "internal ID - target for horizontal escalation");
        check(r, "exp",    getClaim(p, "exp"),    "ABSENT = token NEVER expires (Attack 5)");
        check(r, "iss",    getClaim(p, "iss"),    "ABSENT = no issuer check -> a token from another app would pass");
        check(r, "iat",    getClaim(p, "iat"),    "issued-at; useful for auditing");
    }

    // helper to avoid repeating has()/get()
    private String getClaim(JSONObject obj, String name) {
        if (!obj.has(name)) {
            return null;
        }
        return obj.get(name).toString();
    }

    private void check(AttackResult r, String name, String value, String note) {
        if (value == null) {
            r.warn(name + ": (absent) - " + note);
        } else {
            r.info(name + ": " + value + " - " + note);
        }
    }
}
