package be.itassist.domain.commun;

/**
 * Journalisation applicative. Le contexte est passé en paires clé/valeur pour
 * rester exploitable en recherche ; aucune donnée personnelle ne doit y figurer.
 */
public interface Journal {

    void info(String message, Contexte... contexte);

    void avertissement(String message, Contexte... contexte);

    void erreur(String message, Throwable exception, Contexte... contexte);
}
