package be.itassist.api.infrastructure;

import be.itassist.domain.commun.CodesErreur;
import be.itassist.domain.commun.Contexte;
import be.itassist.domain.commun.Journal;
import be.itassist.domain.commun.Resultat;
import be.itassist.domain.tickets.DepotTickets;
import be.itassist.domain.tickets.Priorite;
import be.itassist.domain.tickets.StatutTicket;
import be.itassist.domain.tickets.Ticket;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Dépôt de tickets adossé au fichier JSONL de démonstration. Implémentation de
 * référence des conventions du projet : Resultat en sortie, aucune exception
 * d'entrée/sortie qui s'échappe, journalisation par Journal.
 */
public final class DepotTicketsFichier implements DepotTickets {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final Path chemin;
    private final Journal journal;
    private volatile List<Ticket> tickets;

    public DepotTicketsFichier(Path chemin, Journal journal) {
        this.chemin = chemin;
        this.journal = journal;
    }

    @Override
    public Resultat<List<Ticket>> lister() {
        List<Ticket> enCache = tickets;
        if (enCache != null) {
            return Resultat.succes(enCache);
        }

        if (!Files.exists(chemin)) {
            journal.erreur("Fichier de tickets absent", null, Contexte.de("chemin", chemin));
            return Resultat.echec(CodesErreur.DEPOT_INDISPONIBLE);
        }

        List<Ticket> lus = new ArrayList<>();
        try {
            for (String ligne : Files.readAllLines(chemin, StandardCharsets.UTF_8)) {
                if (ligne.isBlank()) {
                    continue;
                }

                lus.add(JSON.readValue(ligne, TicketBrut.class).versDomaine());
            }
        } catch (IOException | JacksonException e) {
            journal.erreur("Lecture du fichier de tickets impossible", e, Contexte.de("chemin", chemin));
            return Resultat.echec(CodesErreur.DEPOT_INDISPONIBLE);
        }

        tickets = List.copyOf(lus);
        journal.info("Depot charge", Contexte.de("chemin", chemin), Contexte.de("tickets", lus.size()));
        return Resultat.succes(tickets);
    }

    @Override
    public Resultat<Ticket> parId(String id) {
        Resultat<List<Ticket>> tous = lister();
        if (!tous.estSucces()) {
            return Resultat.echec(tous.codeErreur());
        }

        return tous.valeur().stream()
                .filter(t -> t.id().equals(id))
                .findFirst()
                .map(Resultat::succes)
                .orElseGet(() -> Resultat.echec(CodesErreur.TICKET_INCONNU));
    }

    @Override
    public Resultat<List<Ticket>> parService(String service) {
        Resultat<List<Ticket>> tous = lister();
        if (!tous.estSucces()) {
            return tous;
        }

        List<Ticket> filtres = tous.valeur().stream()
                .filter(t -> t.service().equalsIgnoreCase(service))
                .toList();

        return Resultat.succes(filtres);
    }

    /** Forme brute d'une ligne du fichier, en snake_case. */
    private record TicketBrut(
            String id,
            String titre,
            String categorie,
            String priorite,
            String statut,
            String demandeur,
            String service,
            String site,
            @JsonProperty("ouvert_le") String ouvertLe,
            String description,
            String resolution) {

        Ticket versDomaine() {
            return new Ticket(
                    id,
                    titre,
                    categorie,
                    lirePriorite(priorite),
                    lireStatut(statut),
                    demandeur,
                    service,
                    site,
                    LocalDate.parse(ouvertLe),
                    description,
                    resolution == null || resolution.isBlank() ? null : resolution);
        }

        private static Priorite lirePriorite(String valeur) {
            return switch (valeur) {
                case "Critique" -> Priorite.CRITIQUE;
                case "Haute" -> Priorite.HAUTE;
                case "Basse" -> Priorite.BASSE;
                default -> Priorite.NORMALE;
            };
        }

        private static StatutTicket lireStatut(String valeur) {
            return switch (valeur) {
                case "En cours" -> StatutTicket.EN_COURS;
                case "En attente demandeur" -> StatutTicket.EN_ATTENTE_DEMANDEUR;
                case "Résolu" -> StatutTicket.RESOLU;
                case "Fermé" -> StatutTicket.CLOS;
                default -> StatutTicket.NOUVEAU;
            };
        }
    }
}
