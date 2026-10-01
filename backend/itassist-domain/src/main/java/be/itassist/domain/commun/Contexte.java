package be.itassist.domain.commun;

/**
 * Paire clé/valeur passée au {@link Journal}. Une valeur de contexte est un
 * identifiant ou une mesure, jamais une donnée personnelle.
 */
public record Contexte(String cle, String valeur) {

    public static Contexte de(String cle, String valeur) {
        return new Contexte(cle, valeur);
    }

    public static Contexte de(String cle, Object valeur) {
        return new Contexte(cle, String.valueOf(valeur));
    }
}
