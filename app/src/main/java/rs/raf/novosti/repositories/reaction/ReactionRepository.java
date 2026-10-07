package rs.raf.novosti.repositories.reaction;

import rs.raf.novosti.entities.ReactionType;

import java.util.Map;

public interface ReactionRepository {
    // news reactions
    void addOrUpdateNewsReaction(Long newsId, String sessionId, ReactionType type);
    String getNewsReaction(Long newsId, String sessionId);
    Map<String, Integer> countNewsReactions(Long newsId);

    // comment reactions
    void addOrUpdateCommentReaction(Long commentId, String sessionId, ReactionType type);
    String getCommentReaction(Long commentId, String sessionId);
    Map<String, Integer> countCommentReactions(Long commentId);
}
