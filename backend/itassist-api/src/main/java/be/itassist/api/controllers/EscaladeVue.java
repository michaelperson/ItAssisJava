package be.itassist.api.controllers;

/** Réponse de GET /api/tickets/{id}/escalade. Contrat : interface Escalade de modele.ts. */
public record EscaladeVue(String ticket, String equipe, boolean escaladeRequise, String prioriteCible) {
}
