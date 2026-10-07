package rs.raf.bvs.jwttool.jwt;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.util.Base64;

// manual JWT handling, we deliberately bypass the checks from java-jwt
// we need raw base64url + HMAC so we can build malformed tokens
public final class JwtCodec {

    private JwtCodec() {}

    public static String b64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String b64UrlEncode(String s) {
        return b64UrlEncode(s.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] b64UrlDecode(String s) {
        return Base64.getUrlDecoder().decode(s);
    }

    public static String b64UrlDecodeToString(String s) {
        return new String(b64UrlDecode(s), StandardCharsets.UTF_8);
    }

    public static JwtParts split(String token) {
        if (token == null) throw new IllegalArgumentException("Token is null");
        String[] parts = token.split("\\.", -1); // -1 so it does not drop empty parts
        if (parts.length != 3) {
            throw new IllegalArgumentException("Not a JWT - expected 3 dot-separated parts, got " + parts.length);
        }
        return new JwtParts(parts[0], parts[1], parts[2]);
    }

    public static byte[] hmacSha256(String secret, String signingInput) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC-SHA256 unavailable: " + e.getMessage(), e);
        }
    }

    public static String signHS256(String headerB64, String payloadB64, String secret) {
        byte[] sig = hmacSha256(secret, headerB64 + "." + payloadB64);
        return b64UrlEncode(sig);
    }

    public static String assemble(String headerB64, String payloadB64, String signatureB64) {
        return headerB64 + "." + payloadB64 + "." + signatureB64;
    }
}
