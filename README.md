# ODC Academy — Backend

API REST d’ODC Academy, développée en Java 25 avec Spring Boot, Spring Security, Spring Data JPA, Flyway et PostgreSQL. Le backend gère les comptes, les formations, les cours et les activités d’apprentissage.

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Prérequis](#prérequis)
- [Démarrage local sous Windows](#démarrage-local-sous-windows)
- [Configuration](#configuration)
- [Base de données et migrations](#base-de-données-et-migrations)
- [Domaines de l’API](#domaines-de-lapi)
- [Sécurité et rôles](#sécurité-et-rôles)
- [Fichiers et médias](#fichiers-et-médias)
- [Tests](#tests)
- [Déploiement](#déploiement)
- [Dépannage](#dépannage)

## Fonctionnalités

- Authentification par e-mail/mot de passe, JWT, inscription des apprenants, Google OAuth facultatif et réinitialisation de mot de passe.
- Gestion des profils et des photos.
- Gestion des catégories, des formations, des formateurs et de leurs affectations.
- Création et gestion des cours, modules, leçons texte, PDF et vidéos.
- Inscriptions des apprenants aux cours, progression et quiz.
- Devoirs planifiés, dépôts de fichiers/texte, corrections et suivi.
- Séances en direct organisées par les formateurs avec Jitsi.
- Forums, messagerie, paramètres visuels et logos.

## Prérequis

- Java 25.
- PostgreSQL (base locale utilisée dans le projet : `ODCtest`).
- PowerShell sous Windows.

Le wrapper Maven `mvnw.cmd` est inclus ; Maven n’a pas besoin d’être installé globalement.

## Démarrage local sous Windows

### 1. Créer la base de données

Depuis PowerShell, avec un compte PostgreSQL disposant de `CREATEDB` :

```powershell
psql -U postgres -h localhost -p 5432 -c "CREATE DATABASE ODCtest;"
```

Si la base existe déjà, conservez-la : les migrations Flyway la mettront à jour au démarrage.

### 2. Configurer les variables et démarrer l’API

```powershell
Set-Location .\ODC-Academy-Back

$env:SPRING_PROFILES_ACTIVE = "dev"
$env:DB_URL = "jdbc:postgresql://localhost:5432/ODCtest"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "votre_mot_de_passe_postgres"
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
$env:ODC_ADMIN_EMAIL = "admin@odc.test"
$env:ODC_ADMIN_PASSWORD = Read-Host "Choisissez un mot de passe local fort pour l'admin"

.\mvnw.cmd spring-boot:run
```

L’API est disponible par défaut à `http://localhost:8000`; son préfixe est `/api/v1`.

Le profil `dev` est réservé au développement local. Il active Swagger, mais ne contient aucun mot de passe ou secret JWT par défaut : fournissez `DB_PASSWORD` et `JWT_SECRET` dans l'environnement comme ci-dessus. Les liens de réinitialisation ne sont pas écrits dans les logs; configurez un SMTP de test si vous souhaitez tester cette fonction. Ne l’utilisez pas en production. Le compte admin bootstrap est créé si aucun ADMIN n’existe et si les variables `ODC_ADMIN_EMAIL` et `ODC_ADMIN_PASSWORD` sont valides (8 caractères minimum pour le mot de passe). Ne partagez jamais les valeurs de ces variables.

## Configuration

Le modèle des variables de déploiement est dans [`.env.example`](./.env.example). Spring Boot ne charge pas automatiquement un fichier `.env` : configurez les variables dans l’environnement du processus, le service de lancement ou un gestionnaire de secrets.

| Variable | Description | Valeur locale / comportement |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Profil Spring | `dev` localement uniquement |
| `PORT` | Port HTTP | `8000` |
| `DB_URL` | URL JDBC PostgreSQL | `jdbc:postgresql://localhost:5432/ODCtest` |
| `DB_USERNAME` | Compte PostgreSQL | `postgres` localement |
| `DB_PASSWORD` | Mot de passe PostgreSQL | Requis dans tous les profils |
| `DDL_AUTO` | Mode de gestion Hibernate | `validate` |
| `JWT_SECRET` | Secret de signature JWT | Obligatoire dans tous les profils, 32+ caractères aléatoires |
| `JWT_EXPIRATION_MS` | Durée du JWT | `86400000` ms |
| `ODC_UPLOAD_DIR` | Répertoire des vidéos, documents et dépôts locaux | `uploads` |
| `MEDIA_STORAGE_PROVIDER` | Stockage des images (`local` ou `cloudinary`) | `local` |
| `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` | Identifiants Cloudinary (requis si le fournisseur vaut `cloudinary`) | Vides |
| `CORS_ORIGINS` | Origines frontend permises | `http://localhost:4200` |
| `PUBLIC_URL` | URL publique de l’application | `http://localhost:4200` |
| `ODC_ADMIN_EMAIL` / `ODC_ADMIN_PASSWORD` | Bootstrap du premier admin | Vides par défaut |
| `GOOGLE_CLIENT_ID` | ID client OAuth Google | Vide : connexion Google désactivée |
| `APP_MAIL_ENABLED` | Activation des e-mails | `false` |
| `APP_MAIL_FROM` | Adresse d’expédition | `no-reply@odc-academy.local` |
| `SPRING_MAIL_HOST/PORT/USERNAME/PASSWORD` | Paramètres SMTP | À fournir selon le fournisseur |
| `SWAGGER_ENABLED` | Swagger hors profil `dev` | `false` |
| `SHOW_SQL` | Affichage SQL Hibernate | `false` |

Générez un secret de production avec une source aléatoire cryptographiquement sûre (par exemple `openssl rand -base64 48`). N’inscrivez aucun vrai secret dans le code ou dans Git.

## Base de données et migrations

Les migrations se trouvent dans `src/main/resources/db/migration/` et sont appliquées automatiquement par Flyway au démarrage. Les migrations actuellement présentes sont V1 à V7. Hibernate utilise `ddl-auto=validate` : il vérifie la structure mais ne crée ni ne modifie les tables.

V4 ajoute les métadonnées de création des cours et les séances en direct. Les suppressions de cours/formations utilisent la suppression en cascade des données associées, incluant inscriptions, devoirs, résultats/progressions, discussions concernées et fichiers médias.

Vérification depuis `psql` :

```sql
\c ODCtest
\dt
SELECT version, description, success
FROM public.flyway_schema_history
ORDER BY installed_rank;
```

Avant une migration ou une opération destructive, sauvegardez la base :

```powershell
pg_dump -U postgres -h localhost -p 5432 -Fc -f .\ODCtest-backup.dump ODCtest
```

Ne modifiez jamais une migration déjà appliquée. Ajoutez une nouvelle migration versionnée, par exemple `V8__description.sql`, puis redémarrez le backend pour laisser Flyway l’exécuter. L’option `baseline-on-migrate` ne remplace pas les scripts manquants et ne corrige pas un schéma incompatible.

## Domaines de l’API

URL locale : `http://localhost:8000/api/v1`.

| Préfixe | Domaine |
|---|---|
| `/auth` | Inscription, connexion, OAuth Google, récupération/réinitialisation du mot de passe |
| `/users` et `/profile` | Lecture des profils, mise à jour du profil courant et photo |
| `/categories` | Catégories de formations |
| `/formations` | Catalogue, affectation des formateurs, gestion admin |
| `/courses` | Catalogue, création, modification et suppression des cours |
| `/modules`, `/lessons` | Contenu pédagogique |
| `/enrollments` | Inscriptions aux cours |
| `/assignments` | Devoirs, rendus et corrections |
| `/quizzes`, `/progress`, `/tracking` | Évaluations et suivi pédagogique |
| `/live-sessions` | Planification, ouverture et fin des séances |
| `/forums`, `/messages` | Discussions et messagerie |
| `/media` | Lecture et téléchargement de médias |
| `/settings` | Paramètres d’apparence et logos |

Swagger UI est disponible à `http://localhost:8000/swagger-ui.html` et OpenAPI JSON à `http://localhost:8000/v3/api-docs` en profil `dev`. Hors `dev`, Swagger est désactivé par défaut ; l’activer explicitement avec `SWAGGER_ENABLED=true` si nécessaire.

Les DTO et signatures précises des routes sont documentés par OpenAPI. Les opérations protégées nécessitent un JWT dans l’en-tête `Authorization: Bearer <token>`.

## Sécurité et rôles

- Les inscriptions publiques créent uniquement des comptes **APPRENANT**.
- L’**ADMIN** gère les utilisateurs, catégories et formations, affecte les formateurs et inscrit les apprenants.
- Le **FORMATEUR** gère le contenu des cours dont il est propriétaire ou, pour un cours historique sans propriétaire, un cours de sa formation attribuée.
- L’admin peut consulter les cours, mais ne peut pas modifier ou supprimer directement le contenu pédagogique du formateur.
- Un utilisateur peut modifier ses informations personnelles et sa photo. Le rôle et l’état actif restent contrôlés par l’administration.
- Les mots de passe sont stockés hachés. Les secrets JWT et les mots de passe sont configurés hors code.

## Fichiers et médias

Les vidéos, documents et dépôts sont écrits dans le répertoire défini par `ODC_UPLOAD_DIR`. Par défaut, les images PNG/JPG/WEBP y sont également stockées ; définir `MEDIA_STORAGE_PROVIDER=cloudinary` et fournir les trois variables Cloudinary les envoie vers Cloudinary et conserve les URL dans la base de données. Les fichiers images acceptés sont limités à 2 Mo. Les PDF sont renvoyés en `application/pdf` et `Content-Disposition: inline`; le frontend les récupère comme blob pour les lire dans son lecteur intégré même lorsque l’API est sur une autre origine. Les dépôts de devoirs acceptent PDF/DOC/DOCX/TXT.

En production, placez ce répertoire sur un volume persistant, définissez des permissions minimales et mettez-le dans la politique de sauvegarde. Ne supprimez pas manuellement les fichiers pendant que l’application les référence en base.

## Tests

Depuis la racine du backend :

```powershell
.\mvnw.cmd test
```

Le script d’intégration [`../scripts/smoke-test.sh`](../scripts/smoke-test.sh) nécessite Bash, `curl`, `jq`, un backend actif et une base de test. Il crée des utilisateurs et contenus ; ne l’exécutez pas sur une base de production.

Depuis Git Bash ou Linux/macOS, à la racine du dépôt :

```bash
bash scripts/smoke-test.sh http://localhost:8000/api/v1
```

## Déploiement

1. Créez une base PostgreSQL et un compte applicatif dédiés, avec les privilèges requis.
2. Configurez `DB_*`, `JWT_SECRET`, `CORS_ORIGINS`, `PUBLIC_URL`, `ODC_UPLOAD_DIR` et les paramètres optionnels depuis le gestionnaire de secrets. Pour conserver les images sur Render Free, créez un compte Cloudinary, récupérez **Cloud name**, **API Key** et **API Secret** dans la console Cloudinary, puis ajoutez ces variables dans Render : `MEDIA_STORAGE_PROVIDER=cloudinary`, `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY` et `CLOUDINARY_API_SECRET`. Gardez l'API Secret uniquement côté backend et ne la commitez jamais. Après le redéploiement, les nouvelles images sont envoyées vers Cloudinary ; les anciennes images stockées localement doivent être téléversées à nouveau si elles ont déjà disparu.
3. Ne définissez pas `SPRING_PROFILES_ACTIVE=dev` en production.
4. Démarrez le backend : Flyway applique les migrations puis Hibernate valide le schéma.
5. Configurez un reverse proxy HTTPS : transmettez `/api/` vers le port backend et servez le frontend sur le même domaine si son environnement de production utilise `/api/v1`.
6. Configurez SMTP si les courriels de réinitialisation doivent être envoyés.
7. Maintenez les fichiers téléversés sur un volume persistant sauvegardé.
8. Désactivez Swagger en production sauf besoin opérationnel précis.

Le projet inclut un `Dockerfile` utilisant Java 25 pour la compilation et l’exécution.

## Dépannage

### Le backend refuse de démarrer

- Vérifiez la disponibilité de PostgreSQL, le nom de la base et les valeurs `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
- Vérifiez les premières erreurs Flyway ou Hibernate dans le terminal.
- En production, vérifiez que `JWT_SECRET` est défini.
- Si Hibernate échoue avec une erreur de validation, comparez le schéma réel avec les migrations appliquées ; ne remplacez pas `validate` par `update` pour masquer l’écart.

### Les tables n’apparaissent pas dans pgAdmin

Dans `ODCtest`, développez **Schemas → public → Tables**, puis actualisez la liste. Le journal est dans `public.flyway_schema_history`. Dans Query Tool, utilisez `SELECT` sur cette table ; `\dt` est une commande `psql`, pas une requête SQL.

### Impossible de consulter un média

Pour une image, vérifiez `MEDIA_STORAGE_PROVIDER` et les trois identifiants Cloudinary ; si le fournisseur vaut `local`, contrôlez plutôt que le fichier existe dans le répertoire `ODC_UPLOAD_DIR`. Pour une vidéo, un document ou un dépôt de devoir, vérifiez toujours ce répertoire et son stockage persistant.

## Documentation associée

- [README frontend](../ODC-Academy-Front/ODC-Academy-Front/README.md)
- [Procédures et scénarios de test](../README-TESTS.md)
- [Modèle de configuration](./.env.example)
