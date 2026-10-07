package rs.raf.novosti.services;

import rs.raf.novosti.entities.ReactionType;
import rs.raf.novosti.repositories.reaction.ReactionRepository;

import javax.inject.Inject;
import java.util.Map;

public class ReactionService {

    @Inject
    private ReactionRepository reactionRepository;

    // news
    public void reactToNews(Long newsId, String sessionId, ReactionType type) {
        this.reactionRepository.addOrUpdateNewsReaction(newsId, sessionId, type);
    }

    public Map<String, Integer> newsReactionCounts(Long newsId) {
        return this.reactionRepository.countNewsReactions(newsId);
    }

    public String myNewsReaction(Long newsId, String sessionId) {
        return this.reactionRepository.getNewsReaction(newsId, sessionId);
    }

    // comments
    public void reactToComment(Long commentId, String sessionId, ReactionType type) {
        this.reactionRepository.addOrUpdateCommentReaction(commentId, sessionId, type);
    }

    public Map<String, Integer> commentReactionCounts(Long commentId) {
        return this.reactionRepository.countCommentReactions(commentId);
    }

    public String myCommentReaction(Long commentId, String sessionId) {
        return this.reactionRepository.getCommentReaction(commentId, sessionId);
    }
}
