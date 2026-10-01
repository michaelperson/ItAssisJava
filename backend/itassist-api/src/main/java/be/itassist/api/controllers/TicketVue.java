package be.itassist.api.controllers;

import be.itassist.domain.tickets.Ticket;

/**
 * Représentation d'un ticket exposée par l'API. Contrat attendu par le frontend
 * (frontend/src/app/tickets/modele.ts) : le domaine n'est jamais sérialisé
 * directement.
 */
public record TicketVue(
        String id,
        String titre,
        String categorie,
        String priorite,
        String statut,
        String demandeur,
        String service,
        String site,
        String ouvertLe,
        String description,
        String resolution) {

    public static TicketVue de(Ticket ticket) {
        return new TicketVue(
                ticket.id(),
                ticket.titre(),
                ticket.categorie(),
                ticket.priorite().libelle(),
                ticket.statut().code(),
                ticket.demandeur(),
                ticket.service(),
                ticket.site(),
                ticket.ouvertLe().toString(),
                ticket.description(),
                ticket.resolution());
    }
}
