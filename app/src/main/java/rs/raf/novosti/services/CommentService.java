package rs.raf.novosti.services;

import rs.raf.novosti.entities.Comment;
import rs.raf.novosti.repositories.comment.CommentRepository;
import rs.raf.novosti.repositories.reaction.ReactionRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class CommentService {

    @Inject
    private CommentRepository commentRepository;

    @Inject
    private ReactionRepository reactionRepository;

    private void populateReactions(Comment comment) {
        if (comment == null || comment.getId() == null) return;
        Map<String, Integer> counts = this.reactionRepository.countCommentReactions(comment.getId());
        comment.setLikes(counts.getOrDefault("likes", 0));
        comment.setDislikes(counts.getOrDefault("dislikes", 0));
    }

    public List<Comment> commentsByNews(Long newsId, int page, int pageSize) {
        List<Comment> comments = this.commentRepository.findByNewsId(newsId, page, pageSize);
        for (Comment comment : comments) {
            populateReactions(comment);
        }
        return comments;
    }

    public int commentsByNewsCount(Long newsId) {
        return this.commentRepository.countByNewsId(newsId);
    }

    public Comment addComment(Comment comment) {
        Comment saved = this.commentRepository.save(comment);
        populateReactions(saved);
        return saved;
    }
}
