package rs.raf.novosti.repositories.comment;

import rs.raf.novosti.entities.Comment;

import java.util.List;

public interface CommentRepository {
    List<Comment> findByNewsId(Long newsId, int page, int pageSize);
    int countByNewsId(Long newsId);
    Comment save(Comment comment);
}
