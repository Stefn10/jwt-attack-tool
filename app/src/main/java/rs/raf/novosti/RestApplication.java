package rs.raf.novosti;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import rs.raf.novosti.repositories.user.PostgresUserRepository;
import rs.raf.novosti.repositories.user.UserRepository;
import rs.raf.novosti.repositories.category.PostgresCategoryRepository;
import rs.raf.novosti.repositories.category.CategoryRepository;
import rs.raf.novosti.repositories.news.PostgresNewsRepository;
import rs.raf.novosti.repositories.news.NewsRepository;
import rs.raf.novosti.repositories.tag.PostgresTagRepository;
import rs.raf.novosti.repositories.tag.TagRepository;
import rs.raf.novosti.repositories.comment.PostgresCommentRepository;
import rs.raf.novosti.repositories.comment.CommentRepository;
import rs.raf.novosti.repositories.reaction.PostgresReactionRepository;
import rs.raf.novosti.repositories.reaction.ReactionRepository;
import rs.raf.novosti.repositories.visit.PostgresVisitRepository;
import rs.raf.novosti.repositories.visit.VisitRepository;

import rs.raf.novosti.services.*;

import javax.inject.Singleton;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class RestApplication extends ResourceConfig {

    public RestApplication() {
        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

        AbstractBinder binder = new AbstractBinder() {
            @Override
            protected void configure() {
                // Repositories
                this.bind(PostgresUserRepository.class).to(UserRepository.class).in(Singleton.class);
                this.bind(PostgresCategoryRepository.class).to(CategoryRepository.class).in(Singleton.class);
                this.bind(PostgresNewsRepository.class).to(NewsRepository.class).in(Singleton.class);
                this.bind(PostgresTagRepository.class).to(TagRepository.class).in(Singleton.class);
                this.bind(PostgresCommentRepository.class).to(CommentRepository.class).in(Singleton.class);
                this.bind(PostgresReactionRepository.class).to(ReactionRepository.class).in(Singleton.class);
                this.bind(PostgresVisitRepository.class).to(VisitRepository.class).in(Singleton.class);

                // Services
                this.bindAsContract(UserService.class);
                this.bindAsContract(CategoryService.class);
                this.bindAsContract(NewsService.class);
                this.bindAsContract(CommentService.class);
                this.bindAsContract(ReactionService.class);
                this.bindAsContract(VisitService.class);
            }
        };
        register(binder);

        packages("rs.raf.novosti");
    }
}
