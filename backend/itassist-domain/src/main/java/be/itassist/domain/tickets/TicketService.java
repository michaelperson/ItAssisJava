package be.itassist.domain.tickets;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service de tickets historique. Ajouté au fil des demandes depuis la reprise
 * du portail interne. Point d'entrée unique utilisé par l'API, par le rapport
 * mensuel et par un script de nuit.
 */
public class TicketService {

    private static List<Ticket> cache;
    private static LocalDateTime cacheLe;

    public String cheminFichier = "data/tickets.jsonl";

    public List<Ticket> chargerTout() {
        if (cache != null && Duration.between(cacheLe, LocalDateTime.now()).toMinutes() < 15) {
            return cache;
        }

        Path chemin = Path.of(cheminFichier);
        if (!Files.exists(chemin)) {
            throw new RuntimeException("Fichier de tickets introuvable : " + cheminFichier);
        }

        List<String> lignes;
        try {
            lignes = Files.readAllLines(chemin, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        List<Ticket> liste = new ArrayList<>();
        for (String ligne : lignes) {
            if (ligne == null || ligne.isBlank()) {
                continue;
            }

            // Lecture « à la main » : le service date d'avant l'ajout d'une
            // bibliothèque JSON au projet.
            String prio = champ(ligne, "priorite");
            Priorite p = Priorite.NORMALE;
            if (prio.equals("Critique")) p = Priorite.CRITIQUE;
            if (prio.equals("Haute")) p = Priorite.HAUTE;
            if (prio.equals("Basse")) p = Priorite.BASSE;

            String st = champ(ligne, "statut");
            StatutTicket s = StatutTicket.NOUVEAU;
            if (st.equals("En cours")) s = StatutTicket.EN_COURS;
            if (st.equals("En attente demandeur")) s = StatutTicket.EN_ATTENTE_DEMANDEUR;
            if (st.equals("Résolu")) s = StatutTicket.RESOLU;
            if (st.equals("Fermé")) s = StatutTicket.CLOS;

            String resolution = champ(ligne, "resolution");
            if (resolution.equals("")) {
                resolution = null;
            }

            liste.add(new Ticket(
                    champ(ligne, "id"),
                    champ(ligne, "titre"),
                    champ(ligne, "categorie"),
                    p,
                    s,
                    champ(ligne, "demandeur"),
                    champ(ligne, "service"),
                    champ(ligne, "site"),
                    LocalDate.parse(champ(ligne, "ouvert_le")),
                    champ(ligne, "description"),
                    resolution));
        }

        cache = liste;
        cacheLe = LocalDateTime.now();
        System.out.println("[TicketService] " + liste.size() + " tickets charges depuis " + cheminFichier);
        return liste;
    }

    public Ticket get(String id) {
        for (Ticket t : chargerTout()) {
            if (t.id().equals(id)) {
                return t;
            }
        }

        throw new RuntimeException("Ticket inconnu : " + id);
    }

    public List<Ticket> rechercher(String texte, String service, String categorie, String priorite, boolean ouvertsSeulement) {
        List<Ticket> res = chargerTout();

        if (texte != null && !texte.equals("")) {
            res = res.stream().filter(t -> t.titre().toLowerCase().contains(texte.toLowerCase())
                    || t.description().toLowerCase().contains(texte.toLowerCase())
                    || (t.resolution() != null && t.resolution().toLowerCase().contains(texte.toLowerCase())))
                    .collect(Collectors.toList());
        }

        if (service != null && !service.equals("")) {
            res = res.stream().filter(t -> t.service().equals(service)).collect(Collectors.toList());
        }

        if (categorie != null && !categorie.equals("")) {
            res = res.stream().filter(t -> t.categorie().equals(categorie)).collect(Collectors.toList());
        }

        if (priorite != null && !priorite.equals("")) {
            if (priorite.equals("Critique")) res = res.stream().filter(t -> t.priorite() == Priorite.CRITIQUE).collect(Collectors.toList());
            else if (priorite.equals("Haute")) res = res.stream().filter(t -> t.priorite() == Priorite.HAUTE).collect(Collectors.toList());
            else if (priorite.equals("Normale")) res = res.stream().filter(t -> t.priorite() == Priorite.NORMALE).collect(Collectors.toList());
            else if (priorite.equals("Basse")) res = res.stream().filter(t -> t.priorite() == Priorite.BASSE).collect(Collectors.toList());
        }

        if (ouvertsSeulement) {
            res = res.stream().filter(t -> t.statut() != StatutTicket.RESOLU && t.statut() != StatutTicket.CLOS).collect(Collectors.toList());
        }

        return res;
    }

    public Map<String, Integer> statistiquesParCategorie() {
        Map<String, Integer> d = new HashMap<>();
        for (Ticket t : chargerTout()) {
            if (d.containsKey(t.categorie())) d.put(t.categorie(), d.get(t.categorie()) + 1);
            else d.put(t.categorie(), 1);
        }

        return d;
    }

    public Map<String, Integer> statistiquesParSite() {
        Map<String, Integer> d = new HashMap<>();
        for (Ticket t : chargerTout()) {
            if (d.containsKey(t.site())) d.put(t.site(), d.get(t.site()) + 1);
            else d.put(t.site(), 1);
        }

        return d;
    }

    public String rapportMensuelTexte(int mois, int annee) {
        StringBuilder sb = new StringBuilder();
        sb.append("RAPPORT MENSUEL " + mois + "/" + annee).append("\n");
        sb.append("Genere le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        sb.append("-".repeat(60)).append("\n");

        List<Ticket> tickets = chargerTout().stream()
                .filter(t -> t.ouvertLe().getMonthValue() == mois && t.ouvertLe().getYear() == annee)
                .collect(Collectors.toList());
        sb.append("Tickets ouverts sur la periode : " + tickets.size()).append("\n");

        Map<String, List<Ticket>> groupes = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::categorie, LinkedHashMap::new, Collectors.toList()));
        List<Map.Entry<String, List<Ticket>>> tries = new ArrayList<>(groupes.entrySet());
        tries.sort((a, b) -> b.getValue().size() - a.getValue().size());

        for (Map.Entry<String, List<Ticket>> groupe : tries) {
            sb.append("  " + String.format("%-35s", groupe.getKey()) + groupe.getValue().size()).append("\n");
            for (Ticket t : groupe.getValue()) {
                if (t.priorite() == Priorite.CRITIQUE) {
                    sb.append("      CRITIQUE " + t.id() + " " + t.titre() + " (" + t.demandeur() + ")").append("\n");
                }
            }
        }

        long ouverts = tickets.stream().filter(t -> t.statut() != StatutTicket.RESOLU && t.statut() != StatutTicket.CLOS).count();
        sb.append("-".repeat(60)).append("\n");
        sb.append("Encore ouverts : " + ouverts + " (" + (tickets.size() == 0 ? 0 : ouverts * 100 / tickets.size()) + " %)").append("\n");

        return sb.toString();
    }

    public void viderCache() {
        cache = null;
    }

    private static String champ(String ligne, String nom) {
        Matcher m = Pattern.compile("\"" + nom + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(ligne);
        if (!m.find()) {
            return "";
        }
        return m.group(1).replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\");
    }
}
