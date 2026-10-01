package be.itassist.domain.tickets;

/** Statut d'un ticket. Le cycle de vie est décrit dans docs/regles-metier.md §1. */
public enum StatutTicket {
    NOUVEAU("Nouveau"),
    EN_COURS("EnCours"),
    EN_ATTENTE_DEMANDEUR("EnAttenteDemandeur"),
    RESOLU("Resolu"),
    CLOS("Ferme");

    private final String code;

    StatutTicket(String code) {
        this.code = code;
    }

    /** Code exposé par l'API, tel qu'attendu par le frontend (tickets/modele.ts). */
    public String code() {
        return code;
    }

    public boolean estTermine() {
        return this == RESOLU || this == CLOS;
    }
}
