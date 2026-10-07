package rs.raf.novosti.resources;

import rs.raf.novosti.entities.Comment;
import rs.raf.novosti.services.CommentService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/news/{newsId}/comments")
public class CommentResource {

    @Inject
    private CommentService commentService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response all(@PathParam("newsId") Long newsId,
                        @QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        List<Comment> comments = this.commentService.commentsByNews(newsId, page, pageSize);
        int total = this.commentService.commentsByNewsCount(newsId);

        Map<String, Object> response = new HashMap<>();
        response.put("comments", comments);
        response.put("totalCount", total);
        response.put("page", page);
        response.put("pageSize", pageSize);

        return Response.ok(response).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("newsId") Long newsId, @Valid Comment comment) {
        comment.setNewsId(newsId);
        Comment created = this.commentService.addComment(comment);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
}
