package rs.raf.novosti.resources;

import rs.raf.novosti.entities.ReactionType;
import rs.raf.novosti.services.ReactionService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/reactions")
public class ReactionResource {

    @Inject
    private ReactionService reactionService;

    // ==================== News reactions ====================

    @POST
    @Path("/news/{newsId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reactToNews(@PathParam("newsId") Long newsId, Map<String, String> body) {
        String sessionId = body.get("sessionId");
        ReactionType type = ReactionType.valueOf(body.get("type").toUpperCase());

        this.reactionService.reactToNews(newsId, sessionId, type);
        return Response.ok(this.reactionService.newsReactionCounts(newsId)).build();
    }

    @GET
    @Path("/news/{newsId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response newsReactions(@PathParam("newsId") Long newsId) {
        return Response.ok(this.reactionService.newsReactionCounts(newsId)).build();
    }

    @GET
    @Path("/news/{newsId}/mine")
    @Produces(MediaType.APPLICATION_JSON)
    public Response myNewsReaction(@PathParam("newsId") Long newsId,
                                   @QueryParam("sessionId") String sessionId) {
        String reaction = this.reactionService.myNewsReaction(newsId, sessionId);
        return Response.ok("{\"reaction\":\"" + (reaction != null ? reaction : "") + "\"}").build();
    }

    // ==================== Comment reactions ====================

    @POST
    @Path("/comments/{commentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reactToComment(@PathParam("commentId") Long commentId, Map<String, String> body) {
        String sessionId = body.get("sessionId");
        ReactionType type = ReactionType.valueOf(body.get("type").toUpperCase());

        this.reactionService.reactToComment(commentId, sessionId, type);
        return Response.ok(this.reactionService.commentReactionCounts(commentId)).build();
    }

    @GET
    @Path("/comments/{commentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response commentReactions(@PathParam("commentId") Long commentId) {
        return Response.ok(this.reactionService.commentReactionCounts(commentId)).build();
    }

    @GET
    @Path("/comments/{commentId}/mine")
    @Produces(MediaType.APPLICATION_JSON)
    public Response myCommentReaction(@PathParam("commentId") Long commentId,
                                      @QueryParam("sessionId") String sessionId) {
        String reaction = this.reactionService.myCommentReaction(commentId, sessionId);
        return Response.ok("{\"reaction\":\"" + (reaction != null ? reaction : "") + "\"}").build();
    }
}
