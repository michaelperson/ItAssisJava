package be.itassist.domain.commun;

/**
 * Codes d'erreur du domaine. Aucun littéral de code d'erreur n'est écrit
 * ailleurs dans le projet : l'API les transpose en codes HTTP.
 */
public final class CodesErreur {

    public static final String TICKET_INCONNU = "TICKET_INCONNU";
    public static final String TRANSITION_INTERDITE = "TRANSITION_INTERDITE";
    public static final String SERVICE_INCONNU = "SERVICE_INCONNU";
    public static final String PRIORITE_INVALIDE = "PRIORITE_INVALIDE";
    public static final String DONNEE_MANQUANTE = "DONNEE_MANQUANTE";
    public static final String DEPOT_INDISPONIBLE = "DEPOT_INDISPONIBLE";

    private CodesErreur() {
    }
}
