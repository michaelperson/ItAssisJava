package be.itassist.api.controllers;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint de vérification de l'état de santé de l'API. */
@RestController
public final class SanteController {

    @GetMapping("/sante")
    public Map<String, String> sante() {
        return Map.of("statut", "ok");
    }
}
