package rs.raf.novosti.resources;

import rs.raf.novosti.services.VisitService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/news/{newsId}/visit")
public class VisitResource {

    @Inject
    private VisitService visitService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response recordVisit(@PathParam("newsId") Long newsId, Map<String, String> body) {
        String sessionId = body.get("sessionId");
        boolean isNew = this.visitService.recordVisit(newsId, sessionId);
        return Response.ok("{\"newVisit\":" + isNew + "}").build();
    }
}
