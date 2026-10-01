package be.itassist.doubles;

import be.itassist.domain.commun.Horloge;
import java.time.LocalDateTime;

public final class FauxHorloge implements Horloge {

    private LocalDateTime maintenant;

    public FauxHorloge(LocalDateTime maintenant) {
        this.maintenant = maintenant;
    }

    @Override
    public LocalDateTime maintenant() {
        return maintenant;
    }

    public void regler(LocalDateTime valeur) {
        this.maintenant = valeur;
    }
}
