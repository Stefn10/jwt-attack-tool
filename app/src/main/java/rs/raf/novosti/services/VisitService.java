package rs.raf.novosti.services;

import rs.raf.novosti.repositories.news.NewsRepository;
import rs.raf.novosti.repositories.visit.VisitRepository;

import javax.inject.Inject;

public class VisitService {

    @Inject
    private VisitRepository visitRepository;

    @Inject
    private NewsRepository newsRepository;

    public boolean recordVisit(Long newsId, String sessionId) {
        boolean isNew = this.visitRepository.addVisit(newsId, sessionId);
        if (isNew) {
            this.newsRepository.incrementVisits(newsId);
        }
        return isNew;
    }
}
