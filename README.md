# boutique-bdd
Projet final BDD
# Boutique en ligne : PostgreSQL + Redis

Projet final « Bases de données relationnelles et non relationnelles ».
Projet individuel. Auteur : Faridatou AMADOU.

## Présentation

Application de boutique en ligne : catalogue de produits, panier, commandes et paiements.
Deux bases aux rôles distincts :

- PostgreSQL(relationnelle) : source de vérité pour les clients, produits, commandes, lignes de commande et paiements. Contraintes d'intégrité, transactions, vue, trigger de stock, rôle applicatif à droits limités.
- Redis  : cache du détail produit , classement des produits les plus consultés  et panier .

L'API Spring Boot relie les deux. Deux endpoints croisent les bases : le classement des produits populaires ids dans Redis, détails dans PostgreSQL et la validation de commande panier lu dans Redis, commande écrite dans PostgreSQL en une transaction.

## Pile technique
Composant        Technologie 

Base      relationnelle       PostgreSQL 16 
Base      non relationnelle    Redis 7 
API  Java 21, Spring Boot (Web, Data JPA, Data Redis, Validation)  
Frontend  Page HTML/JavaScript servie par l'API 
 Infrastructure  Docker Compose 

## Prérequis

- Docker Desktop (ou Docker Engine + Compose v2)
- Aucun autre outil : Java et Maven sont dans l'image de l'API.

## Démarrage (une seule commande)

```bash
cp .env.example .env        
docker compose --env-file .env -f docker/docker-compose.yml up -d --build --wait
```

Au premier lancement, PostgreSQL exécute les scripts de `docker/init-sql/` : schéma, vue et trigger, rôle applicatif, peuplement (500 produits, 2 000 clients, 5 000 commandes, 12 500 lignes, environ 3 500 paiements) et index.

Pour repartir de zéro : `docker compose --env-file .env -f docker/docker-compose.yml down -v`, puis relancer la commande ci-dessus.

Les mots de passe de `.env.example` sont des valeurs de démonstration. Aucun secret réel n'est versionné ; `.env` est ignoré par git.

## Adresses

Service           URL 

Frontend          http://localhost:8080/ 

API               http://localhost:8080/api/produits 

PostgreSQL        localhost:5432, base `boutique` 

Redis          localhost:6379 

Si un port est déjà pris sur votre machine, modifiez `API_PORT`, `POSTGRES_PORT` ou `REDIS_PORT` dans `.env`.

**Comptes de démonstration** : il n'y a pas d'authentification. Les clients numérotés de 1 à 2000 existent ; les exemples utilisent le client 1.

## Endpoints principaux

| Méthode | Chemin | Rôle |

| GET | `/api/produits?page=0&size=20` | Catalogue paginé (PostgreSQL) |
| GET | `/api/produits/{id}` | Détail : cache Redis, sinon PostgreSQL. En-tête `X-Cache` : `HIT` ou `MISS` |
| POST | `/api/produits` | Création (corps JSON validé) |
| PUT | `/api/produits/{id}` | Modification, invalide le cache |
| DELETE | `/api/produits/{id}` | Suppression, invalide le cache et le classement |
| GET | `/api/produits/populaires?n=5` | Classement Redis croisé avec PostgreSQL |
| GET | `/api/clients/{id}/panier` | Panier Redis enrichi avec les prix PostgreSQL |
| PUT | `/api/clients/{id}/panier/{produitId}?quantite=2` | Définit une quantité (0 retire la ligne) |
| DELETE | `/api/clients/{id}/panier` | Vide le panier |
|POST | `/api/clients/{id}/commandes` | Valide la commande : `{"methode":"CARTE"}` (CARTE, MOBILE_MONEY, VIREMENT, ESPECES) |

Codes d'erreur : 400 (données invalides, panier vide), 404 (ressource absente), 409 (stock insuffisant ou référence inexistante).

## Vérification rapide

```bash
curl -i http://localhost:8080/api/produits/1        # 1re lecture : X-Cache: MISS
curl -i http://localhost:8080/api/produits/1        # 2e lecture : X-Cache: HIT
curl -X PUT "http://localhost:8080/api/clients/1/panier/7?quantite=2"
curl -X POST http://localhost:8080/api/clients/1/commandes -H "Content-Type: application/json" -d '{"methode":"CARTE"}'
```

Sous PowerShell, utilisez `curl.exe` ou `Invoke-RestMethod`.

## Structure du dépôt
| GET | /api/statistiques/categories | Chiffre d'affaires par catégorie (lit la vue PostgreSQL) |