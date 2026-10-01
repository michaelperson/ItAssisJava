package be.itassist.domain.commun;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Implémentation par défaut de {@link Horloge}. Seule classe autorisée à lire l'horloge système. */
public final class HorlogeSysteme implements Horloge {

    @Override
    public LocalDateTime maintenant() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
