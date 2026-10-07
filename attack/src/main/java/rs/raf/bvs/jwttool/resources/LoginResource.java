package rs.raf.bvs.jwttool.resources;

import org.json.JSONObject;
import rs.raf.bvs.jwttool.http.ApiClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/login")
public class LoginResource {

    public static class LoginRequest {
        public String baseUrl;
        public String email;
        public String password;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest req) {
        Map<String, Object> body = new HashMap<>();
        try {
            String baseUrl = (req.baseUrl != null && !req.baseUrl.isEmpty())
                    ? req.baseUrl : "http://localhost:8080/rva/api";
            ApiClient api = new ApiClient(baseUrl);
            String jwt = api.login(req.email, req.password);

            JSONObject ok = new JSONObject().put("jwt", jwt).put("baseUrl", baseUrl);
            return Response.ok(ok.toString()).build();
        } catch (Exception e) {
            body.put("error", e.getMessage());
            return Response.status(401).entity(body).build();
        }
    }
}
