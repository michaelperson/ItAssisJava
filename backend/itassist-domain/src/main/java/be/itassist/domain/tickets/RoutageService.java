package be.itassist.domain.tickets;

import be.itassist.domain.commun.CodesErreur;
import be.itassist.domain.commun.Contexte;
import be.itassist.domain.commun.Horloge;
import be.itassist.domain.commun.Journal;
import be.itassist.domain.commun.Resultat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Règles d'escalade. Un ticket dont le délai de traitement dépasse l'engagement
 * associé à sa priorité doit être escaladé au niveau supérieur.
 */
public final class RoutageService {

    // Engagement de prise en charge, exprimé en jours ouvrés.
    private static final Map<Priorite, Integer> ENGAGEMENT_JOURS_OUVRES = new EnumMap<>(Map.of(
            Priorite.CRITIQUE, 2,
            Priorite.HAUTE, 1,
            Priorite.NORMALE, 5,
            Priorite.BASSE, 10));

    // Clés en minuscules : la recherche est insensible à la casse.
    private static final Map<String, String> SERVICE_PAR_CATEGORIE = Map.of(
            "accès et authentification", "Support N1",
            "erp et applications métier", "Applications métier",
            "poste de travail", "Support N1",
            "réseau et téléphonie", "Infrastructure",
            "données et reporting", "Applications métier",
            "sécurité", "Sécurité opérationnelle");

    private final Horloge horloge;
    private final Journal journal;

    public RoutageService(Horloge horloge, Journal journal) {
        this.horloge = horloge;
        this.journal = journal;
    }

    public Resultat<String> equipeCible(Ticket ticket) {
        String equipe = SERVICE_PAR_CATEGORIE.get(ticket.categorie().toLowerCase(Locale.ROOT));
        if (equipe == null) {
            journal.avertissement(
                    "Categorie sans equipe de rattachement",
                    Contexte.de("ticket", ticket.id()),
                    Contexte.de("categorie", ticket.categorie()));

            return Resultat.echec(CodesErreur.SERVICE_INCONNU);
        }

        return Resultat.succes(equipe);
    }

    public Resultat<Boolean> doitEscalader(Ticket ticket) {
        if (ticket.statut().estTermine()) {
            return Resultat.succes(false);
        }

        Integer engagement = ENGAGEMENT_JOURS_OUVRES.get(ticket.priorite());
        if (engagement == null) {
            return Resultat.echec(CodesErreur.PRIORITE_INVALIDE);
        }

        LocalDate aujourdhui = horloge.maintenant().toLocalDate();
        int ecoules = joursOuvresEntre(ticket.ouvertLe(), aujourdhui);
        boolean escalade = ecoules > engagement;

        journal.info(
                "Evaluation escalade",
                Contexte.de("ticket", ticket.id()),
                Contexte.de("priorite", ticket.priorite()),
                Contexte.de("engagement", engagement),
                Contexte.de("joursOuvresEcoules", ecoules),
                Contexte.de("escalade", escalade));

        return Resultat.succes(escalade);
    }

    public Resultat<Priorite> prioriteApresEscalade(Ticket ticket) {
        Resultat<Boolean> doit = doitEscalader(ticket);
        if (!doit.estSucces()) {
            return Resultat.echec(doit.codeErreur());
        }

        if (!doit.valeur() || ticket.priorite() == Priorite.CRITIQUE) {
            return Resultat.succes(ticket.priorite());
        }

        return Resultat.succes(ticket.priorite().suivante());
    }

    // TODO(dette connue) : les jours fériés belges ne sont pas pris en compte.
    // Une table de jours fériés est prévue mais non implémentée.
    public static int joursOuvresEntre(LocalDate debut, LocalDate fin) {
        if (!fin.isAfter(debut)) {
            return 0;
        }

        int compte = 0;
        for (LocalDate jour = debut; jour.isBefore(fin); jour = jour.plusDays(1)) {
            DayOfWeek jourSemaine = jour.getDayOfWeek();
            if (jourSemaine != DayOfWeek.SATURDAY && jourSemaine != DayOfWeek.SUNDAY) {
                compte++;
            }
        }

        return compte;
    }
}
