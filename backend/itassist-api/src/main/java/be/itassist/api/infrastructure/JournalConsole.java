package be.itassist.api.infrastructure;

import be.itassist.domain.commun.Contexte;
import be.itassist.domain.commun.Journal;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implémentation de Journal adossée à SLF4J. Le format de sortie est
 * « message | cle=valeur | cle=valeur » pour rester lisible en recherche texte.
 */
public final class JournalConsole implements Journal {

    private static final Logger LOGGER = LoggerFactory.getLogger(JournalConsole.class);

    @Override
    public void info(String message, Contexte... contexte) {
        LOGGER.info("{}", formater(message, contexte));
    }

    @Override
    public void avertissement(String message, Contexte... contexte) {
        LOGGER.warn("{}", formater(message, contexte));
    }

    @Override
    public void erreur(String message, Throwable exception, Contexte... contexte) {
        LOGGER.error(formater(message, contexte), exception);
    }

    private static String formater(String message, Contexte[] contexte) {
        if (contexte.length == 0) {
            return message;
        }
        return message + " | " + Arrays.stream(contexte)
                .map(c -> c.cle() + "=" + c.valeur())
                .collect(Collectors.joining(" | "));
    }
}
