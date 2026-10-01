package be.itassist.domain.tickets;

/**
 * Priorité d'un ticket. L'ordre de déclaration est significatif : il va de la
 * plus basse à la plus haute, et l'escalade s'appuie sur cet ordre.
 */
public enum Priorite {
    BASSE("Basse"),
    NORMALE("Normale"),
    HAUTE("Haute"),
    CRITIQUE("Critique");

    private final String libelle;

    Priorite(String libelle) {
        this.libelle = libelle;
    }

    public String libelle() {
        return libelle;
    }

    /** Niveau immédiatement supérieur. {@code CRITIQUE} n'a pas de niveau supérieur. */
    public Priorite suivante() {
        return this == CRITIQUE ? CRITIQUE : values()[ordinal() + 1];
    }

    @Override
    public String toString() {
        return libelle;
    }
}
