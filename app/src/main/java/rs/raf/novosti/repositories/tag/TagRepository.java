package rs.raf.novosti.repositories.tag;

import rs.raf.novosti.entities.Tag;

import java.util.List;

public interface TagRepository {
    Tag findOrCreate(String value);
    List<Tag> findByNewsId(Long newsId);
    void addTagToNews(Long newsId, Long tagId);
    void removeTagsFromNews(Long newsId);
}
