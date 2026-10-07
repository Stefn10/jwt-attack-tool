package rs.raf.novosti.resources;

import rs.raf.novosti.entities.News;
import rs.raf.novosti.services.NewsService;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/news")
public class NewsResource {

    @Inject
    private NewsService newsService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response all(@QueryParam("page") @DefaultValue("1") int page,
                        @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        List<News> newsList = this.newsService.allNews(page, pageSize);
        int total = this.newsService.newsCount();

        Map<String, Object> response = new HashMap<>();
        response.put("news", newsList);
        response.put("totalCount", total);
        response.put("page", page);
        response.put("pageSize", pageSize);

        return Response.ok(response).build();
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Response search(@QueryParam("q") @DefaultValue("") String q,
                           @QueryParam("page") @DefaultValue("1") int page,
                           @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        if (q.trim().isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Query parameter 'q' is required");
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        List<News> newsList = this.newsService.searchNews(q, page, pageSize);
        int total = this.newsService.searchCount(q);

        Map<String, Object> response = new HashMap<>();
        response.put("news", newsList);
        response.put("totalCount", total);
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("query", q);

        return Response.ok(response).build();
    }

    @GET
    @Path("/most-read")
    @Produces(MediaType.APPLICATION_JSON)
    public Response mostRead(@QueryParam("limit") @DefaultValue("10") int limit,
                             @QueryParam("days") @DefaultValue("30") int days) {
        List<News> newsList = this.newsService.mostReadNews(limit, days);
        return Response.ok(newsList).build();
    }

    @GET
    @Path("/top-reactions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response topReactions(@QueryParam("limit") @DefaultValue("3") int limit) {
        List<News> newsList = this.newsService.topByReactions(limit);
        return Response.ok(newsList).build();
    }

    @GET
    @Path("/by-tag/{value}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response byTag(@PathParam("value") String value,
                          @QueryParam("page") @DefaultValue("1") int page,
                          @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        List<News> newsList = this.newsService.newsByTag(value, page, pageSize);
        int total = this.newsService.newsByTagCount(value);

        Map<String, Object> response = new HashMap<>();
        response.put("news", newsList);
        response.put("totalCount", total);
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("tag", value);

        return Response.ok(response).build();
    }

    @GET
    @Path("/by-category/{categoryId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response byCategory(@PathParam("categoryId") Long categoryId,
                               @QueryParam("page") @DefaultValue("1") int page,
                               @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        List<News> newsList = this.newsService.newsByCategory(categoryId, page, pageSize);
        int total = this.newsService.newsByCategoryCount(categoryId);

        Map<String, Object> response = new HashMap<>();
        response.put("news", newsList);
        response.put("totalCount", total);
        response.put("page", page);
        response.put("pageSize", pageSize);
        response.put("categoryId", categoryId);

        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("id") Long id) {
        News news = this.newsService.findNews(id);
        if (news == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\":\"News not found\"}")
                    .build();
        }
        return Response.ok(news).build();
    }

    @GET
    @Path("/{id}/related")
    @Produces(MediaType.APPLICATION_JSON)
    public Response related(@PathParam("id") Long id,
                            @QueryParam("limit") @DefaultValue("3") int limit) {
        List<News> related = this.newsService.relatedNews(id, limit);
        return Response.ok(related).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(Map<String, Object> body) {
        News news = new News();
        news.setTitle((String) body.get("title"));
        news.setText((String) body.get("text"));
        news.setAuthorId(Long.valueOf(body.get("authorId").toString()));
        news.setCategoryId(Long.valueOf(body.get("categoryId").toString()));

        List<String> tagValues = (List<String>) body.get("tags");

        News created = this.newsService.addNews(news, tagValues);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("id") Long id, Map<String, Object> body) {
        News newsData = new News();
        newsData.setTitle((String) body.get("title"));
        newsData.setText((String) body.get("text"));
        newsData.setCategoryId(Long.valueOf(body.get("categoryId").toString()));

        List<String> tagValues = (List<String>) body.get("tags");

        News updated = this.newsService.updateNews(id, newsData, tagValues);
        if (updated == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\":\"News not found\"}")
                    .build();
        }

        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") Long id) {
        this.newsService.deleteNews(id);
        return Response.ok("{\"message\":\"News deleted\"}").build();
    }
}
