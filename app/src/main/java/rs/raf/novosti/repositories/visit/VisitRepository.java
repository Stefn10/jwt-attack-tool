package rs.raf.novosti.repositories.visit;

public interface VisitRepository {
    boolean addVisit(Long newsId, String sessionId);
    boolean hasVisited(Long newsId, String sessionId);
}
