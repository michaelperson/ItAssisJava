package be.itassist.domain.commun;

import java.time.LocalDateTime;

/**
 * Source de temps injectée. Le domaine n'appelle jamais {@code LocalDateTime.now()},
 * {@code LocalDate.now()} ni {@code Instant.now()} directement : les règles de
 * délai doivent être testables.
 */
public interface Horloge {

    /** Date et heure courantes, en UTC. */
    LocalDateTime maintenant();
}
