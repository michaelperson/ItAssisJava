package be.itassist.domain.commun;

/**
 * Résultat d'une opération métier. Le domaine ne lève pas d'exception pour un
 * cas de gestion prévu : il renvoie un échec porteur d'un code d'erreur.
 * Aucune méthode du domaine ne renvoie {@code null} ni {@code Optional} à la
 * place d'un échec.
 *
 * @param <T> type de la valeur en cas de succès
 */
public final class Resultat<T> {

    private final T valeur;
    private final boolean succes;
    private final String codeErreur;

    private Resultat(T valeur, boolean succes, String codeErreur) {
        this.valeur = valeur;
        this.succes = succes;
        this.codeErreur = codeErreur;
    }

    public static <T> Resultat<T> succes(T valeur) {
        return new Resultat<>(valeur, true, "");
    }

    public static <T> Resultat<T> echec(String codeErreur) {
        return new Resultat<>(null, false, codeErreur);
    }

    public boolean estSucces() {
        return succes;
    }

    public String codeErreur() {
        return codeErreur;
    }

    /**
     * Valeur du résultat. Lire la valeur d'un échec est une erreur de
     * programmation, pas un cas de gestion : d'où l'exception.
     */
    public T valeur() {
        if (!succes) {
            throw new IllegalStateException(
                    "Lecture de la valeur d'un résultat en échec (" + codeErreur + ").");
        }
        return valeur;
    }
}
