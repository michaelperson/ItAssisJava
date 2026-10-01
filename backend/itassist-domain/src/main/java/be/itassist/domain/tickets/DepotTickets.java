package be.itassist.domain.tickets;

import be.itassist.domain.commun.Resultat;
import java.util.List;

/**
 * Accès aux tickets. Toute méthode d'entrée/sortie renvoie un {@link Resultat} :
 * aucune {@code IOException} ne traverse cette interface, aucune méthode ne
 * renvoie {@code null} ni {@code Optional}. Un incident d'accès devient
 * {@code DEPOT_INDISPONIBLE}.
 */
public interface DepotTickets {

    Resultat<Ticket> parId(String id);

    Resultat<List<Ticket>> lister();

    Resultat<List<Ticket>> parService(String service);
}
