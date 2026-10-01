package be.itassist.domain.tickets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import be.itassist.domain.commun.CodesErreur;
import be.itassist.domain.commun.Resultat;
import be.itassist.doubles.FabriqueTicket;
import be.itassist.doubles.FauxHorloge;
import be.itassist.doubles.JournalMuet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * Convention de nommage : methode_contexte_resultatAttendu.
 * Un test valide une seule règle et n'utilise qu'une seule assertion logique.
 */
class RoutageServiceTest {

    private static RoutageService service(LocalDateTime maintenant) {
        return new RoutageService(new FauxHorloge(maintenant), new JournalMuet());
    }

    @Test
    void equipeCible_categorieSecurite_renvoieSecuriteOperationnelle() {
        RoutageService service = service(LocalDateTime.of(2026, 3, 10, 0, 0));
        Ticket ticket = FabriqueTicket.nominal().avecCategorie("Sécurité").creer();

        Resultat<String> resultat = service.equipeCible(ticket);

        assertTrue(resultat.estSucces());
        assertEquals("Sécurité opérationnelle", resultat.valeur());
    }

    @Test
    void equipeCible_categorieInconnue_echoueAvecServiceInconnu() {
        RoutageService service = service(LocalDateTime.of(2026, 3, 10, 0, 0));
        Ticket ticket = FabriqueTicket.nominal().avecCategorie("Cantine").creer();

        Resultat<String> resultat = service.equipeCible(ticket);

        assertFalse(resultat.estSucces());
        assertEquals(CodesErreur.SERVICE_INCONNU, resultat.codeErreur());
    }

    @Test
    void joursOuvresEntre_semaineComplete_ignoreLeWeekEnd() {
        // Lundi 2 mars 2026 -> lundi 9 mars 2026 : 5 jours ouvrés.
        int compte = RoutageService.joursOuvresEntre(
                LocalDate.of(2026, 3, 2),
                LocalDate.of(2026, 3, 9));

        assertEquals(5, compte);
    }

    @Test
    void doitEscalader_ticketClos_renvoieFaux() {
        RoutageService service = service(LocalDateTime.of(2026, 6, 1, 0, 0));
        Ticket ticket = FabriqueTicket.nominal()
                .avecStatut(StatutTicket.CLOS)
                .ouvertLe(LocalDate.of(2026, 1, 5))
                .creer();

        Resultat<Boolean> resultat = service.doitEscalader(ticket);

        assertTrue(resultat.estSucces());
        assertFalse(resultat.valeur());
    }
}
