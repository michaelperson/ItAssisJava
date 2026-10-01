package be.itassist.domain.tickets;

import java.time.LocalDate;

/**
 * Ticket du service d'assistance. Type immuable : toute évolution d'état passe
 * par une copie produite par le domaine.
 *
 * @param resolution {@code null} tant que le ticket n'est pas résolu
 */
public record Ticket(
        String id,
        String titre,
        String categorie,
        Priorite priorite,
        StatutTicket statut,
        String demandeur,
        String service,
        String site,
        LocalDate ouvertLe,
        String description,
        String resolution) {
}
