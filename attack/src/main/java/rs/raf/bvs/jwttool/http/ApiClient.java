package rs.raf.bvs.jwttool.http;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// thin HTTP wrapper around the RAF Novosti API
// uses HttpURLConnection, no external library
public final class ApiClient {

    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = stripTrailingSlash(baseUrl);
    }

    public String baseUrl() {
        return baseUrl;
    }

    public static class HttpResult {
        public final int status;
        public final String body;
        // response headers, they used to be null so now they are populated
        // old code that does not touch this field still works normally
        public final Map<String, List<String>> headers;

        public HttpResult(int status, String body) {
            this(status, body, Collections.<String, List<String>>emptyMap());
        }

        public HttpResult(int status, String body, Map<String, List<String>> headers) {
            this.status = status;
            this.body = body;
            this.headers = headers != null ? headers : Collections.<String, List<String>>emptyMap();
        }

        // return the first header value, case-insensitive
        // because getHeaderFields keeps the original names from the server
        public String header(String name) {
            if (name == null || headers == null) return null;
            for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                if (e.getKey() != null && e.getKey().equalsIgnoreCase(name)) {
                    List<String> vals = e.getValue();
                    if (vals != null && !vals.isEmpty()) return vals.get(0);
                }
            }
            return null;
        }
    }

    public String login(String email, String password) throws Exception {
        JSONObject body = new JSONObject().put("email", email).put("password", password);
        HttpResult res = post("/users/login", body.toString(), null);
        if (res.status != 200) {
            throw new RuntimeException("Login failed (HTTP " + res.status + "): " + res.body);
        }
        JSONObject json = new JSONObject(res.body);
        if (!json.has("jwt")) {
            throw new RuntimeException("Login response missing 'jwt' field: " + res.body);
        }
        return json.getString("jwt");
    }

    public HttpResult get(String path, String bearerToken) throws Exception {
        HttpURLConnection conn = open(path, "GET", bearerToken);
        return finish(conn);
    }

    public HttpResult post(String path, String jsonBody, String bearerToken) throws Exception {
        HttpURLConnection conn = open(path, "POST", bearerToken);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }
        return finish(conn);
    }

    public HttpResult delete(String path, String bearerToken) throws Exception {
        HttpURLConnection conn = open(path, "DELETE", bearerToken);
        return finish(conn);
    }

    // OPTIONS preflight for the CORS audit (Attack 10)
    // sends Origin + Access-Control-Request-* and returns all headers so we can inspect the CORS policy
    public HttpResult optionsPreflight(String path, String origin, String requestMethod) throws Exception {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("OPTIONS");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        if (origin != null) {
            conn.setRequestProperty("Origin", origin);
        }
        if (requestMethod != null) {
            conn.setRequestProperty("Access-Control-Request-Method", requestMethod);
            conn.setRequestProperty("Access-Control-Request-Headers", "authorization,content-type");
        }
        return finish(conn);
    }

    private HttpURLConnection open(String path, String method, String bearerToken) throws Exception {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        if (bearerToken != null) {
            conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
        }
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }

    private HttpResult finish(HttpURLConnection conn) throws Exception {
        int status = conn.getResponseCode();
        // we read the headers before the body, after disconnect they are unreliable
        Map<String, List<String>> hdrs = conn.getHeaderFields();
        InputStream is = (status >= 200 && status < 400) ? conn.getInputStream() : conn.getErrorStream();
        String body = readAll(is);
        conn.disconnect();
        return new HttpResult(status, body, hdrs);
    }

    private String readAll(InputStream is) throws Exception {
        if (is == null) return "";
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
            return sb.toString().trim();
        }
    }

    private static String stripTrailingSlash(String s) {
        return (s != null && s.endsWith("/")) ? s.substring(0, s.length() - 1) : s;
    }
}
