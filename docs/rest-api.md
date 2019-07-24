# API Rest disponible

Différentes fonctionnalités de Datagate sont disponibles sous la forme d'API REST. Elles permettent d'exploiter une gestion de configuration sans passer par l'ui disponible (par exemple depuis un outil tiers).

La spécification de l'API est disponible au format OpenAPI (Swagger 2+) par défaut sur le point d'entré `/v2/api-docs`

## Pour accéder à l'API Rest

L'API est par défaut disponible via une UI Swagger 2 standard : une fois connecté, cliquer sur le bouton "API REST" en haut à droite.

> Une **authentification** peut être nécessaire pour certains services. **L'authentification est peu sécurisée**, et s'appuie uniquement sur un identifiant token unique par utilisateur. Le token est disponible sur la page de détail de chaque utilisateur, et doit être définie en paramètre d'url `token` (ou directement sur l'interface Swagger proposée).

> Par ailleurs les opérations sont généralement réalisées dans "le projet actif de l'utilisateur". En plus de l'authentification cela implique donc d'avoir spécifié le *bon* projet actif courant pour l'utilisateur, via l'API d'édition des projets (voir en fin de cette page).

## Services disponibles

### Fonctions de base de l'application / *Application Api Controller*

Les fonctions proposées ici sont génériques et permettent quelques opérations d'administration de l'application.

**Services disponibles :**

* `get /rest/v1/app/` : Fourni le nom et la version de l'instance active
* `get /rest/v1/app/processes` - `authentifié` : Liste les jobs assynchrones actifs de l'utilisateur courant. Les merges et diff en cours de préparation sont par exemple des jobs asynchrones. D'autres types de jobs peuvent être gérés dans de futures versions de l'application
* `post /rest/v1/app/processes` - `authentifié` : Permet de killer un job asynchrone spécifié pour l'utilisateur courant. Il est ainsi possible d'interrompre un diff ou un merge "bloqué" sans accéder à l'application
* `get /rest/v1/app/state` : Etat général de l'application (HEALTHCHECK)


### Fonctions de gestion du dictionnaire / *Dictionary Api Controller*

Le dictionnaire définie les tables de paramètrage. Il est versionné. L'API REST permet de spécifier un dictionnaire par import, et de mettre à jour les versions.

**Services disponibles :**

* `post /rest/v1/dictionary/upload` - `authentifié` : Importe un fichier .par de dictionnaire. Utilisé en particuliers par le générateur de dictionnaire par API de code, qui produit un fichier .par directement compatible, et peut directement l'uploader sur une instance cible via ce point d'entrée
* `get /rest/v1/dictionary/version` - `authentifié` : Donne la version courante du dictionnaire
* `put /rest/v1/dictionary/version/{name}` - `authentifié` : Créé une nouvelle version courante du dictionnaire
* `get /rest/v1/dictionary/versions` - `authentifié` : Liste toutes les versions connues du dictionnaire

### Fonctions de gestion du backlog (index, lots ...) / *Backlog Api Controller*

Des fonctions de création de nouveaux lots sont disponibles. Les opérations de consultation ou d'export nécessitent de passer par l'UI.

**Services disponibles :**

* `post /rest/v1/backlog/diff` - `authentifié` : Démarre un nouveau diff (préparation de lot) asynchrone dans le projet actif de l'utilisateur.
* `post /rest/v1/backlog/upload` - `authentifié` : Upload un fichier de lots ".par" et démarre un merge (préparation de lot de merge) asynchrone
* `get /rest/v1/backlog/status` - `authentifié` : Obtention du statut du diff ou du merge en cours. Si le diff est prêt, le statut est alors `COMMIT_CAN_PREPARE`
* `post /rest/v1/backlog/cancel` - `authentifié` : Annule le diff ou le merge en cours
* `get /rest/v1/backlog/details` - `authentifié` : Fourni le détail du contenu du lot (après diff ou merge) quand il a le statut `COMMIT_CAN_PREPARE`. Si le statut est différent aucun résultat n'est fourni
* `post /rest/v1/backlog/commit` - `authentifié` : Approuve tout le contenu du diff ou du merge préparé de statut `COMMIT_CAN_PREPARE`, spécifie un commentaire de lot, et sauvegarde le lot. Si des changements sont à réaliser sur la bdd managée, ils sont également appliqués. Le traitement est transactionnel (non asynchrone) et peut donc être assez long, attention au timeout.

### Feature flipping / *Features Api Controller*

Certains comportements de l'application peuvent être modifiés "à chaud" grace à un système de feature-flipping. Les codes de features supportés sont :

* `SELECT_PK_AS_DEFAULT_DICT_ENTRY_KEY` 
* `VALIDATE_VERSION_FOR_IMPORT`: Permet de désactiver la validation de version du dictionnaire lors de l'import d'un lot. **Permet donc d'appliquer un lot avec un dictionnaire non cohérent : attention !**
* `CHECK_MISSING_IDS_AT_MANAGED_UPDATE`
* `CHECK_MISSING_IDS_AT_MANAGED_DELETE`

**Services disponibles :**

* `get /rest/v1/features/` - `authentifié` : Liste les features et leurs états pour l'application
* `post /rest/v1/features/disable/{feature}` - `authentifié` : Désactive la feature indiquée
* `post /rest/v1/features/enable/{feature}` - `authentifié` : Active la feature indiquée

### Gestion des projets / *Project Api Controller*

Par défaut tous les utilisateurs sont dans le projet unique si il y en a un seul. Si plusieurs projets existent, il faut sélectionner le projet actif soit depuis l'UI, soit avec ces services. Dictionnaire et lots sont gérés par projet.

**Services disponibles :**

* `get /rest/v1/projects/active` - `authentifié` : Donne le projet actif pour l'utilisateur
* `post /rest/v1/projects/active` - `authentifié` : Selectionne un autre projet actif pour l'utilisateur 
* `get /rest/v1/projects/all` - `authentifié` : Liste tous les projets disponibles pour l'utilisateur (= les projets "préférés") parmis lesquels il est possible de choisir le projet actif.
