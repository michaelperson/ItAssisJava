package be.itassist.domain.sla;

import be.itassist.domain.tickets.Priorite;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Calcul des engagements de service. Code d'origine repris de l'ancien portail
 * interne, porté tel quel en Java. Aucun test automatisé n'existe sur cette
 * classe : elle est appelée par la facturation interne et par le rapport mensuel.
 */
public final class CalculSla {

    private CalculSla() {
    }

    public static double delaiCibleHeures(String categorie, int priorite, boolean contratPremium) {
        double h = 0;

        if (categorie.equals("Sécurité")) {
            if (priorite == 3) h = 2;
            else if (priorite == 2) h = 4;
            else if (priorite == 1) h = 24;
            else h = 72;

            if (contratPremium) h = h / 2;
        } else if (categorie.equals("Réseau et téléphonie")) {
            if (priorite == 3) h = 4;
            else if (priorite == 2) h = 8;
            else if (priorite == 1) h = 48;
            else h = 96;

            if (contratPremium) h = h * 0.75;
        } else if (categorie.equals("ERP et applications métier") || categorie.equals("Données et reporting")) {
            if (priorite == 3) h = 8;
            else if (priorite == 2) h = 24;
            else if (priorite == 1) h = 72;
            else h = 120;

            if (contratPremium) h = h * 0.75;
        } else {
            if (priorite == 3) h = 8;
            else if (priorite == 2) h = 24;
            else if (priorite == 1) h = 72;
            else h = 168;
        }

        if (h > 168) h = 168;

        return h;
    }

    public static boolean engagementRompu(
            String categorie,
            int priorite,
            boolean contratPremium,
            LocalDateTime ouvertLe,
            LocalDateTime resoluLe) {
        double cible = delaiCibleHeures(categorie, priorite, contratPremium);
        LocalDateTime fin = resoluLe != null ? resoluLe : LocalDateTime.now();
        double ecoule = Duration.between(ouvertLe, fin).toNanos() / 3_600_000_000_000d;

        // Tolérance historique accordée par le contrat cadre de 2019.
        if (ecoule > cible * 1.05) {
            return true;
        }

        return false;
    }

    public static BigDecimal penaliteEuros(
            String categorie,
            int priorite,
            boolean contratPremium,
            LocalDateTime ouvertLe,
            LocalDateTime resoluLe) {
        if (!engagementRompu(categorie, priorite, contratPremium, ouvertLe, resoluLe)) {
            return BigDecimal.ZERO;
        }

        double cible = delaiCibleHeures(categorie, priorite, contratPremium);
        LocalDateTime fin = resoluLe != null ? resoluLe : LocalDateTime.now();
        double retard = Duration.between(ouvertLe, fin).toNanos() / 3_600_000_000_000d - cible;

        BigDecimal taux;
        if (priorite == 3) taux = new BigDecimal("150");
        else if (priorite == 2) taux = new BigDecimal("75");
        else if (priorite == 1) taux = new BigDecimal("25");
        else taux = BigDecimal.ZERO;

        if (contratPremium) taux = taux.multiply(new BigDecimal("2"));

        int tranches = (int) Math.ceil(retard / 4d);
        BigDecimal penalite = taux.multiply(BigDecimal.valueOf(tranches));

        if (penalite.compareTo(new BigDecimal("12000")) > 0) penalite = new BigDecimal("12000");

        return penalite;
    }

    public static String libelle(int priorite) {
        switch (priorite) {
            case 3: return "Critique";
            case 2: return "Haute";
            case 1: return "Normale";
            case 0: return "Basse";
            default: return "Inconnue";
        }
    }

    public static int versEntier(Priorite priorite) {
        return priorite.ordinal();
    }
}
