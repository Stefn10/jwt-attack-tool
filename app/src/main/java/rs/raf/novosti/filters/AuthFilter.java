package rs.raf.novosti.filters;

import rs.raf.novosti.services.UserService;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Provider
public class AuthFilter implements ContainerRequestFilter {

    @Inject
    private UserService userService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if (!this.isAuthRequired(requestContext)) {
            return;
        }

        try {
            String token = requestContext.getHeaderString("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.replace("Bearer ", "");
            }

            if (token == null || token.isEmpty() || !this.userService.isAuthorized(token)) {
                requestContext.abortWith(this.unauthorizedResponse());
            }
        } catch (Exception e) {
            requestContext.abortWith(this.unauthorizedResponse());
        }
    }

    private boolean isAuthRequired(ContainerRequestContext req) {
        String path = req.getUriInfo().getPath();
        String method = req.getMethod();


        if (path.contains("login")) {
            return false;
        }

        if (method.equals("GET")) {
            if (path.startsWith("news") ||
                path.startsWith("categories") ||
                path.startsWith("reactions")) {
                return false;
            }
        }

        if (method.equals("POST")) {
            if (path.startsWith("news/") && path.endsWith("/comments")) {
                return false;
            }
            if (path.startsWith("news/") && path.endsWith("/visit")) {
                return false;
            }
            if (path.startsWith("reactions/")) {
                return false;
            }
        }

        return true;
    }

    private Response unauthorizedResponse() {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Unauthorized");
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
