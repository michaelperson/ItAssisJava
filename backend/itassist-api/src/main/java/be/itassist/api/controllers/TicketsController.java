package be.itassist.api.controllers;

import be.itassist.domain.commun.CodesErreur;
import be.itassist.domain.commun.Contexte;
import be.itassist.domain.commun.Journal;
import be.itassist.domain.commun.Resultat;
import be.itassist.domain.tickets.DepotTickets;
import be.itassist.domain.tickets.Priorite;
import be.itassist.domain.tickets.RoutageService;
import be.itassist.domain.tickets.Ticket;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public final class TicketsController {

    private final DepotTickets depot;
    private final RoutageService routage;
    private final Journal journal;

    public TicketsController(DepotTickets depot, RoutageService routage, Journal journal) {
        this.depot = depot;
        this.routage = routage;
        this.journal = journal;
    }

    /** Liste tous les tickets, ou les tickets d'un service. */
    @GetMapping
    public ResponseEntity<?> lister(@RequestParam(required = false) String service) {
        Resultat<List<Ticket>> resultat = service == null || service.isBlank()
                ? depot.lister()
                : depot.parService(service);

        return resultat.estSucces()
                ? ResponseEntity.ok(resultat.valeur().stream().map(TicketVue::de).toList())
                : versReponse(resultat.codeErreur());
    }

    /** Récupère un ticket par son identifiant. */
    @GetMapping("/{id}")
    public ResponseEntity<?> parId(@PathVariable String id) {
        Resultat<Ticket> resultat = depot.parId(id);
        return resultat.estSucces()
                ? ResponseEntity.ok(TicketVue.de(resultat.valeur()))
                : versReponse(resultat.codeErreur());
    }

    /** Consulte les informations d'escalade d'un ticket. */
    @GetMapping("/{id}/escalade")
    public ResponseEntity<?> escalade(@PathVariable String id) {
        Resultat<Ticket> ticket = depot.parId(id);
        if (!ticket.estSucces()) {
            return versReponse(ticket.codeErreur());
        }

        Resultat<String> equipe = routage.equipeCible(ticket.valeur());
        Resultat<Boolean> doit = routage.doitEscalader(ticket.valeur());
        Resultat<Priorite> cible = routage.prioriteApresEscalade(ticket.valeur());

        if (!doit.estSucces() || !cible.estSucces()) {
            return versReponse(doit.estSucces() ? cible.codeErreur() : doit.codeErreur());
        }

        journal.info("Escalade consultee", Contexte.de("ticket", id), Contexte.de("escalade", doit.valeur()));

        return ResponseEntity.ok(new EscaladeVue(
                ticket.valeur().id(),
                equipe.estSucces() ? equipe.valeur() : null,
                doit.valeur(),
                cible.valeur().libelle()));
    }

    /**
     * Transposition unique des codes du domaine vers HTTP. Aucun contrôleur ne
     * construit de réponse d'erreur autrement.
     */
    private static ResponseEntity<Map<String, String>> versReponse(String codeErreur) {
        HttpStatus statut = switch (codeErreur) {
            case CodesErreur.TICKET_INCONNU, CodesErreur.SERVICE_INCONNU -> HttpStatus.NOT_FOUND;
            case CodesErreur.PRIORITE_INVALIDE, CodesErreur.DONNEE_MANQUANTE -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.SERVICE_UNAVAILABLE;
        };
        return ResponseEntity.status(statut).body(Map.of("code", codeErreur));
    }
}
