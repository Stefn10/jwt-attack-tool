package rs.raf.bvs.jwttool.attacks;

import rs.raf.bvs.jwttool.http.ApiClient;

public class AttackContext {

    private String token;
    private String secret;
    private String baseUrl;
    private String testEndpoint;
    private String newRole;

    public ApiClient api() {
        return new ApiClient(baseUrl != null ? baseUrl : "http://localhost:8080/rva/api");
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getTestEndpoint() { return testEndpoint; }
    public void setTestEndpoint(String testEndpoint) { this.testEndpoint = testEndpoint; }

    public String getNewRole() { return newRole; }
    public void setNewRole(String newRole) { this.newRole = newRole; }
}
