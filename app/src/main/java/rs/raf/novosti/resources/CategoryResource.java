package rs.raf.novosti.resources;

import rs.raf.novosti.entities.Category;
import rs.raf.novosti.services.CategoryService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/categories")
public class CategoryResource {

    @Inject
    private CategoryService categoryService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response all(@QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        List<Category> categories = this.categoryService.allCategories(page, pageSize);
        int total = this.categoryService.categoryCount();

        Map<String, Object> response = new HashMap<>();
        response.put("categories", categories);
        response.put("totalCount", total);
        response.put("page", page);
        response.put("pageSize", pageSize);

        return Response.ok(response).build();
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response allNoPagination() {
        return Response.ok(this.categoryService.allCategories()).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("id") Long id) {
        Category category = this.categoryService.findCategory(id);
        if (category == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\":\"Category not found\"}")
                    .build();
        }
        return Response.ok(category).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@Valid Category category) {
        Category created = this.categoryService.addCategory(category);

        if (created == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "A category with this name already exists");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Long id, @Valid Category categoryData) {
        Category existing = this.categoryService.findCategory(id);
        if (existing == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Category not found");
            return Response.status(Response.Status.NOT_FOUND).entity(error).build();
        }

        Category updated = this.categoryService.updateCategory(id, categoryData);

        if (updated == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Name already taken by another category");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = this.categoryService.deleteCategory(id);

        if (!deleted) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Cannot delete category that contains news");
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        return Response.ok("{\"message\":\"Category deleted\"}").build();
    }
}
