package be.itassist.api;

import be.itassist.api.infrastructure.DepotTicketsFichier;
import be.itassist.api.infrastructure.JournalConsole;
import be.itassist.domain.commun.Horloge;
import be.itassist.domain.commun.HorlogeSysteme;
import be.itassist.domain.commun.Journal;
import be.itassist.domain.tickets.DepotTickets;
import be.itassist.domain.tickets.RoutageService;
import be.itassist.domain.tickets.TicketService;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Assemblage des briques du domaine. Le domaine ne connaît pas Spring : toutes
 * ses classes sont déclarées ici, en singletons.
 */
@Configuration
public class AssemblageApplication implements WebMvcConfigurer {

    @Bean
    public Horloge horloge() {
        return new HorlogeSysteme();
    }

    @Bean
    public Journal journal() {
        return new JournalConsole();
    }

    @Bean
    public DepotTickets depotTickets(@Value("${tickets.chemin}") String chemin, Journal journal) {
        return new DepotTicketsFichier(Path.of(chemin).toAbsolutePath().normalize(), journal);
    }

    @Bean
    public RoutageService routageService(Horloge horloge, Journal journal) {
        return new RoutageService(horloge, journal);
    }

    @Bean
    public TicketService ticketService() {
        return new TicketService();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200")
                .allowedHeaders("*")
                .allowedMethods("*");
    }
}
