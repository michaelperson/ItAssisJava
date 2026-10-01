package be.itassist.doubles;

import be.itassist.domain.tickets.Priorite;
import be.itassist.domain.tickets.StatutTicket;
import be.itassist.domain.tickets.Ticket;
import java.time.LocalDate;

/**
 * Fabrique de tickets de test. Les tests ne construisent jamais un Ticket
 * directement : ils partent du cas nominal et surchargent le seul champ utile.
 *
 * <pre>{@code
 * Ticket t = FabriqueTicket.nominal().avecPriorite(Priorite.HAUTE).creer();
 * }</pre>
 */
public final class FabriqueTicket {

    private String id = "INC-2026-0001";
    private String categorie = "Poste de travail";
    private Priorite priorite = Priorite.NORMALE;
    private StatutTicket statut = StatutTicket.EN_COURS;
    private LocalDate ouvertLe = LocalDate.of(2026, 3, 2);

    private FabriqueTicket() {
    }

    public static FabriqueTicket nominal() {
        return new FabriqueTicket();
    }

    public FabriqueTicket avecId(String valeur) {
        this.id = valeur;
        return this;
    }

    public FabriqueTicket avecCategorie(String valeur) {
        this.categorie = valeur;
        return this;
    }

    public FabriqueTicket avecPriorite(Priorite valeur) {
        this.priorite = valeur;
        return this;
    }

    public FabriqueTicket avecStatut(StatutTicket valeur) {
        this.statut = valeur;
        return this;
    }

    public FabriqueTicket ouvertLe(LocalDate valeur) {
        this.ouvertLe = valeur;
        return this;
    }

    public Ticket creer() {
        return new Ticket(
                id,
                "Titre de test",
                categorie,
                priorite,
                statut,
                "Demandeur de test",
                "Comptabilité",
                "Namur",
                ouvertLe,
                "Description de test",
                null);
    }
}
