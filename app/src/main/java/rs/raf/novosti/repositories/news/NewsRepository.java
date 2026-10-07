package rs.raf.novosti.repositories.news;

import rs.raf.novosti.entities.News;

import java.util.List;

public interface NewsRepository {
    News findById(Long id);
    List<News> findAll(int page, int pageSize);
    int count();
    News save(News news);
    News update(News news);
    void delete(Long id);
    void incrementVisits(Long newsId);

    List<News> search(String query, int page, int pageSize);
    int countSearch(String query);

    List<News> findMostRead(int limit, int days);

    List<News> findTopByReactions(int limit);

    List<News> findRelatedByTags(Long newsId, int limit);

    List<News> findByTag(String tagValue, int page, int pageSize);
    int countByTag(String tagValue);

    List<News> findByCategory(Long categoryId, int page, int pageSize);
    int countByCategory(Long categoryId);
}
