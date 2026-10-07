package rs.raf.bvs.jwttool.jwt;

import org.json.JSONObject;

// unpacked JWT, holds the raw base64 strings (for the signature)
// and the decoded JSON (for reading and editing)
public final class JwtParts {

    public final String headerB64;
    public final String payloadB64;
    public final String signatureB64;
    public final JSONObject header;
    public final JSONObject payload;

    public JwtParts(String headerB64, String payloadB64, String signatureB64) {
        this.headerB64 = headerB64;
        this.payloadB64 = payloadB64;
        this.signatureB64 = signatureB64;
        this.header = new JSONObject(JwtCodec.b64UrlDecodeToString(headerB64));
        this.payload = new JSONObject(JwtCodec.b64UrlDecodeToString(payloadB64));
    }

    public String signingInput() {
        return headerB64 + "." + payloadB64;
    }

    public byte[] signatureBytes() {
        return JwtCodec.b64UrlDecode(signatureB64);
    }
}
