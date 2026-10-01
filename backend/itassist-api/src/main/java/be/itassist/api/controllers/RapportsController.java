package be.itassist.api.controllers;

import be.itassist.domain.tickets.TicketService;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de reporting. Encore adossés au TicketService historique.
 */
@RestController
@RequestMapping("/api/rapports")
public class RapportsController {

    private final TicketService service;

    public RapportsController(TicketService service) {
        this.service = service;
    }

    /**
     * Génère un rapport mensuel au format texte.
     *
     * @param mois Mois (1-12)
     * @param annee Année
     * @return Rapport mensuel
     */
    @GetMapping("/mensuel")
    public ResponseEntity<String> mensuel(@RequestParam int mois, @RequestParam int annee) {
        try {
            String texte = service.rapportMensuelTexte(mois, annee);
            return ResponseEntity.ok()
                    .contentType(new MediaType("text", "plain", java.nio.charset.StandardCharsets.UTF_8))
                    .body(texte);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(ex.getMessage());
        }
    }

    /**
     * Retourne les statistiques des tickets par catégorie.
     *
     * @return Statistiques par catégorie
     */
    @GetMapping("/par-categorie")
    public Map<String, Integer> parCategorie() {
        return service.statistiquesParCategorie();
    }

    /**
     * Retourne les statistiques des tickets par site.
     *
     * @return Statistiques par site
     */
    @GetMapping("/par-site")
    public Map<String, Integer> parSite() {
        return service.statistiquesParSite();
    }

    /**
     * Recherche des tickets selon plusieurs critères.
     *
     * @param texte Texte de recherche (optionnel)
     * @param service Service (optionnel)
     * @param categorie Catégorie (optionnel)
     * @param priorite Priorité (optionnel)
     * @param ouvertsSeulement Afficher seulement les tickets ouverts
     * @return Résultats de recherche
     */
    @GetMapping("/recherche")
    public List<TicketVue> recherche(
            @RequestParam(required = false) String texte,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) String priorite,
            @RequestParam(defaultValue = "false") boolean ouvertsSeulement) {
        return this.service.rechercher(texte, service, categorie, priorite, ouvertsSeulement)
                .stream().map(TicketVue::de).toList();
    }
}
