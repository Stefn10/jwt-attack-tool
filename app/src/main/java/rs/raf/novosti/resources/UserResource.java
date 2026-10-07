package rs.raf.novosti.resources;

import rs.raf.novosti.entities.User;
import rs.raf.novosti.requests.CreateUserRequest;
import rs.raf.novosti.requests.LoginRequest;
import rs.raf.novosti.services.UserService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/users")
public class UserResource {

    @Inject
    private UserService userService;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Valid LoginRequest loginRequest) {
        Map<String, String> response = new HashMap<>();

        String jwt = this.userService.login(loginRequest.getEmail(), loginRequest.getPassword());
        if (jwt == null) {
            response.put("error", "Invalid email or password, or the account is deactivated");
            return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
        }

        response.put("jwt", jwt);
        return Response.ok(response).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response all(@QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        List<User> users = this.userService.allUsers(page, pageSize);
        int total = this.userService.userCount();

        Map<String, Object> response = new HashMap<>();
        response.put("users", users);
        response.put("totalCount", total);
        response.put("page", page);
        response.put("pageSize", pageSize);

        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("id") Long id) {
        User user = this.userService.findUser(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\":\"User not found\"}")
                    .build();
        }
        return Response.ok(user).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@Valid CreateUserRequest request) {
        // password and confirmPassword must match (required by spec)
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Password and confirmation do not match");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        User created = this.userService.addUser(request);

        if (created == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "A user with this email already exists");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Long id, @Valid User userData) {
        User existing = this.userService.findUser(id);
        if (existing == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "User not found");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        User updated = this.userService.updateUser(id, userData);

        if (updated == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Email already taken by another user");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        return Response.ok(updated).build();
    }

    @PUT
    @Path("/{id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response toggleStatus(@PathParam("id") Long id) {
        this.userService.toggleStatus(id);
        return Response.ok("{\"message\":\"Status updated\"}").build();
    }
}
