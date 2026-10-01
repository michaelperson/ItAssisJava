# IT-Assist — dépôt d'entraînement (version Java)

Application de gestion des tickets du service d'assistance interne. Backend
Java 25 / Spring Boot 4, frontend Angular, module historique InterSystems Caché
encore en production.

**Ce dépôt est un support de formation.** L'application est cohérente et
plausible de bout en bout, mais elle n'est pas destinée à être mise en service :
elle existe pour qu'on y travaille avec un assistant d'IA sur du code qui
ressemble à du vrai — avec ses conventions non écrites, sa dette assumée, son
module que plus personne n'ose toucher, et au moins un défaut qui n'a pas été
détecté.

## Arborescence

```
backend/
  pom.xml                    Projet Maven parent (Spring Boot 4, Java 25)
  itassist-domain/           Métier : tickets, routage, engagements de service
                             + tests unitaires — couverture partielle assumée
  itassist-api/              API HTTP, dépôt de données, journalisation
frontend/                    Angular : liste des tickets, tableau de bord
legacy-cache/                Routine ObjectScript en production. Gelée.
data/                        Jeu de tickets synthétique (voir data/README.md)
docs/                        Règles métier — source de vérité fonctionnelle
logs/                        Journal applicatif d'une session réelle
scripts/                     Vide. Outillage produit pendant les ateliers.
.githooks/                   Vide. Crochets Git produits pendant les ateliers.
ateliers/                    Vide. Livrables non-code des ateliers.
```

## Prérequis

- JDK 25 (Temurin, Microsoft Build of OpenJDK ou équivalent) ;
- Maven 3.9 ou plus récent ;
- Node.js 22 pour le frontend.

## Démarrer

```bash
# Compilation et tests
mvn -f backend/pom.xml verify

# Backend — API sur http://localhost:5187
mvn -f backend/pom.xml install -DskipTests
mvn -f backend/itassist-api/pom.xml spring-boot:run

# Frontend — sur http://localhost:4200
cd frontend && npm install && npm start
```

Le backend lit `data/tickets.jsonl`. Le chemin est configurable par
`tickets.chemin` dans `backend/itassist-api/src/main/resources/application.yml`
(relatif au dossier `backend/itassist-api`, d'où `spring-boot:run` démarre).

```bash
curl http://localhost:5187/sante
curl http://localhost:5187/api/tickets/INC-2026-0241
curl http://localhost:5187/api/tickets/INC-2026-0241/escalade
curl "http://localhost:5187/api/rapports/mensuel?mois=8&annee=2026"
```

Documentation interactive de l'API : http://localhost:5187/swagger-ui.html

## Ce qu'il faut savoir avant d'y toucher

Le dépôt ne contient **pas** de fichier de contexte projet. C'est le premier
livrable de la formation, pas un oubli : `CLAUDE.md.template` en est le gabarit
pour un assistant en ligne de commande de type Claude Code, et
`.github/copilot-instructions.md.template` le gabarit équivalent pour GitHub
Copilot. Utilisez celui qui correspond à l'outil du groupe — le contenu attendu
est le même, seul l'emplacement lu automatiquement par l'assistant change.

> Préparation formateur : si `.github/copilot-instructions.md.template` est
> absent après copie du dépôt, déplacez `copilot-instructions.md.template`
> (racine) dans `.github/`. Une fois en place, supprimez la copie de la racine.

Les conventions du projet ne sont écrites nulle part. Elles sont lisibles dans
le code — dans une partie du code. Trois fichiers en particulier sont la
référence de ce que l'équipe considère comme correct :

- `backend/itassist-api/src/main/java/be/itassist/api/infrastructure/DepotTicketsFichier.java`
- `backend/itassist-domain/src/main/java/be/itassist/domain/tickets/RoutageService.java`
- `frontend/src/app/tickets/liste-tickets.component.ts`

D'autres fichiers ne les respectent pas. Faire la différence entre les deux
catégories, sans qu'on vous dise laquelle est laquelle, est le premier exercice.

`docs/regles-metier.md` est la **source de vérité fonctionnelle**. En cas
d'écart entre ce document et le code, c'est le code qui a tort.

## Dette connue et assumée

Ces points sont documentés, décidés, et ne doivent **pas** être « corrigés »
sans discussion :

- Les jours fériés belges ne sont pas pris en compte dans le calcul des jours
  ouvrés (`RoutageService`).
- `CalculSla` est un portage littéral de l'ancien portail interne. Il est appelé
  par la facturation. Aucun test ne le couvre.
- `TicketService` est un service historique qui mélange lecture de fichier,
  règles métier et mise en forme. Il alimente encore le rapport mensuel.
- `legacy-cache/RoutineTicket.mac` est en production et gelé.
- Les dépendances frontend ne sont pas à jour. C'est un choix de calendrier.

Le reste n'est pas de la dette assumée. Le reste est à regarder.

## Périmètre de vérification

Le jeu de données, le journal applicatif et l'outillage Python des ateliers ont
été exécutés et vérifiés.

Le module `itassist-domain` et ses tests ont été **compilés et exécutés** en
Java 25. Le module `itassist-api` a été écrit contre les API de Spring Boot 4.1
et compilé contre leurs signatures, mais la première construction Maven
complète (`mvn verify`, qui télécharge Spring Boot) doit être faite sur un poste
ayant accès à Maven Central, avant la session.

Si `mvn verify` ou `npm run build` signale une erreur, ce n'est donc pas un
exercice caché : c'est une régression, et il faut la signaler.

Note de cohérence : le projet cible Java 25 (`java.version` dans
`backend/pom.xml`). Le gabarit `.gitlab-ci.yml.template` utilise l'image Maven
correspondante. Si vous changez de cible, les deux se changent ensemble.
