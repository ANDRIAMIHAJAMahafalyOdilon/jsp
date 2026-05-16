# Projet 5 - Gestion de reservation (JSP)

Application JSP/Servlet avec Bootstrap 5 pour la gestion des reservations des places d'une cooperative.

## Fonctionnalites
- CRUD `voiture`
- CRUD `client` + recherche `LIKE` (nom/telephone)
- Creation/affichage/mise a jour `reservation`
- Suppression `reservation` avec liberation de place
- Affichage des places libres par voiture
- Generation PDF du recu client
- Export PDF de la liste voyageurs (avec filtres)
- Statistiques paiements + recette totale
- Recherche multi-criteres et tri sur reservations
- Pagination des listes (voitures, clients, reservations)

## Prerequis
- JDK 11+
- Tomcat 9+
- MySQL 8+
- Maven (optionnel pour build WAR)

## Base de donnees
1. Creer la base via `db/schema.sql`.
2. Adapter les acces DB dans `src/main/java/com/cooperative/util/DBConnection.java`.

## Lancement
- Deployer le projet dans Tomcat (WAR ou projet web).
- Ouvrir: `/jsp-reservation-cooperative/dashboard`

## URLs principales
- `/dashboard`
- `/voitures`
- `/clients`
- `/reservations`
- `/receipt?id=RES-XXXX`
