# Gestion du paramètrage Efluid

Prototype d'application dédiée à l'identification, au packaging et au déploiement de paramètrage pour une instance Efluid.

## Avancement général

*Mise à jour au 13/02/2018*

**Maquette statique**
* Dernière version : 03/02/2018. Modification de la gestion des entrées dans le dictionnaire : se fait sur un autre écran de saisie à partir de la liste

**Points techniques**
* Mise en place général : projet actif, stacks en place. BDD Postgres core fonctionnelle
* Modèle core : en place, avec repos
* Utilisation BDD managed : Avec JDBCTemplate. Metadata OK avec PGSQL, a tester sur Oracle
* Preparation au diff : "regénération" des données préues à partir de l'index en place, testé.
* Merge : développé, non testé
* Application / rollback de paramètres : en place, Application testé OK.
* Export / import : dev en place, test seulement sur dictionnaire
* Traitement des dépendances : En place, testé OK
* Un benchmark synthétique des fonctions principale en place. Sera amélioré plus tard
* Les composants de génération "fine" de requêtes SQL ou d'inline de données sont désormais des beans springs extensibles
* Init de données de démo actif. La BDD et des données de tests sont créées automatiquement au démarrage
* Build : maven OK. En attendant d'avoir un vrai service CI acceptant le projet, utilisation d'un build scripté (pipeline *struff*)
* Sécurité : pas traité

**Intégration maquette**
* Mise en place d'un layout pour simpifier les templates Thymeleaf
* index ("/") : page dynamique mais données en dur. Menu et authentification pas géré. 
* édition des domaines fonctionnels ("/domains"): globalement implémenté. 
* édition du dictionnaire ("/dictionary"): globalement implémenté. Quelques bugs, seront traités plus tard
* préparation du diff ("/prepare"): globalement implémenté. Quelques bugs, seront traités plus tard
* listing des commits ("/commits"): en cours
* autres écrans : pas dynamisés / pas intégrés avec thymeleaf

## Utilisation

Le prototype est basé sur Spring-boot. Il n'y a rien à installer pour l'exécuter _par défaut_ :
* Il démarre dans un mode "demo" par défaut
* Il utilise alors une BDD embarquée H2 auto-générée. Attention elle est dropée au moment du stop
* Plus loin dans ce README sont donnés des infos pour utiliser des BDD Postgres pour aller plus loin en terme de validation du comportement. 

Le fichier de configuration technique de l'application est *src/main/resources/application.yml*

### Quickstart
Pour démarrer sans rien installer, juste à partir du projet cloné, en ayant maven sur son poste, utiliser : 

    ## Attention, des dépendances qui ne sont pas présentes dans l'Artifactory Efluid peuvent être nécessaires. 
    ## Idéalement, exécuter la commande en désactivant l'utilisation du repository Efluid dans le fichier 
    ## ~/.m2/settings.xml (en ajoutant un profile dédié pour cela par exemple)
    
    mvn spring-boot:run

L'application démarre après build. Le service est accessible à l'adresse [http://localhost:8080](http://localhost:8080)

### Démarrage depuis un IDE
Pour démarrer depuis un IDE, lancer la classe exécutable __fr.uem.efluid.Application__

### Templating
Les templates sont traités avec Thymeleaf. Ils restent des HTML valides. Ils sont éditables à chaud (tant que le cache de template est désactivé dans application.yml)

### Demo data 
Pour tester l'application avec une instance de BDD réelle, et non pas la BDD embarquée H2, utilisation de Postgres possible. Attention, néant en terme de sécurité, à utiliser juste pour un déploiement local

**Attention, dans tous les cas le mode demo avec la BDD H2 embarquée ne simule pas la BDD de l'application managée (= la BDD de Efluid): elle doit être instanciée par ailleurs.**

#### Instance PG 
Démarrage avec docker

    docker run --name test-postgres -e POSTGRES_PASSWORD=test -p 5432:5432 -v /opt/server/postgresql/data:/var/lib/postgresql/data -d postgres

Puis création de 2 databases : manager et demo. Création d'un user "user"/"user" owner de ces DB

#### Instance Oracle 
Oracle XE peut être utilisé pour représenter l'application demo. 
Démarrage avec docker

    docker run --name oracle -d -p 49161:1521 -e ORACLE_ALLOW_REMOTE=true wnameless/oracle-xe-11g

Se connecter ensuite à l'instance avec SQL Developer avec les paramètres : 
* hostname: ...suivant installation...
* port: 49161
* sid: xe
* username: system
* password: oracle (par defaut)

Puis créer un user "demo"/"demo" avec des droits suffisant pour créer des données

#### Instance demo
Pour intiliser l'instance demo : Elle est utilisée pour représenter une application "managed" dont la gestion du paramètrage serait pilotée avec l'application. 

    -- Le script d'initialisation pour postgres :
    /src/database/model_demo_init_pgsql.sql
    -- ou, pour oracle
    /src/database/model_demo_init_oracle.sql
    
Ce sont les mêmes modèles, les mêmes données pour les 2 scripts

## Conception / Principes
CF support de présentation.

### Stack technique
Spring-boot avec :
* **Spring MVC + Thymeleaf** : Contrôleurs WEB avec templates Thymeleaf + Contrôleurs Rest pour services d'intégration
* **Spring core** : IoC, core contexte
* **(Spring Security)** - accès sécurité très simplifié, à voir
* **Spring data JPA** : Manipulation BDD core, avec JPA, via répository spécifiés seulement sous la forme d'interfaces java
* **Spring JDBC avec Spring Transaction** : Pour les manipulations de la BDD managed, via JDBC directement

Utilise au maximum les conventions Spring-boot pour éliminer tout code technique

### Utilisation des bases de données
Pour le fonctionnement, il y a 2 DB de connectées :
* Celle propre au gestionnaire de paramètrage. Nommée **core** dans le code où elle est référencée. Utilisée avec Spring-data-jpa, avec config par défaut d'EntityManager
* Celle de l'application gérée. Nommée **Managed** dans le code. Utilisée avec JDBCTemplate, avec un transactionManager identifié configuré spécifiquement

### Configuration de l'application
Fichier de configuration *src/main/resources/application.yml*
Des options permises par Spring-boot donnent la possibilité de surcharger la conf depuis un fichier externe, ou des paramètres particuliers via arguments de ligne de commande.

Le fichier est au format YAML.

**Exemple de configuration standard de démonstration**

    ---
    ## PARAMETER MANAGER CONFIG
    param-efluid:
    
       ### BDD "MANAGED" avec JDBC
       managed-datasource:
           driverClassName: org.postgresql.Driver
           url: jdbc:postgresql://localhost/demo
           username: user
           password: user
           
       details:
           ### Sera autogen avec filtrage resource mvn
           version: 1.1.1
    
    ## TECH FEATURES CUSTOM
    spring:
        ### 2 profils prévus : demo, prod
        profiles:
            active: demo
    
        ### En dev permet de recharger à chaud les modifs sur les TPL
        thymeleaf:
            cache: false
    
        ### BDD "CORE" avec JPA
        datasource:
            url: jdbc:postgresql://localhost/manager
            username: user
            password: user
            driver-class-name: org.postgresql.Driver

## Aide au développement / maintenance

### Utilisation des tests automatisés
Le comportement des fonctions clés de l'application est validé par différents tests automatisés, de types tests unitaires et tests d'intégration.
Ces tests sont exécutés automatiquement lors du build avec Maven. Ils peuvent être démarrés manuellement depuis l'IDE directement également

A noter : les tests d'intégrations utilisent une BDD embarquée H2, droppée après chaque exécution (et rollbackée après chaque cas de test). Une console web est accessible pour consulter les données de la BDD H2 utilisée : il suffit d'ajouter un point d'arrêt sur un test dont on souhaite suivre le comportement au niveau BDD, puis d'accéder à la console à l'adresse [http://localhost:8082](http://localhost:8082). Dans l'interface de connexion, utiliser les paramètres de connexion suivants : 
* **url**: jdbc:h2:~\h2;
* **username**: sa
* **password**: *-- laisser vide --*

### Organisation technique de l'application
Organisation en couche simple. 

**Utilisation des terminologies standards suivantes** :
* xRepository => DAO. Archetype Spring
* xService => Service business. Archetype Spring
* xController => Contrôleur dans le cadre d'un traitement MVC (Spring-MVC est un framework de traitement par action). Archetype Spring
* contenus du package "modele" => Modèle de données business, associé à des entités gérés (en BDD ou en mémoire)
* contenus du package "services.types" => TO utilisés par les services business
* contenus du package "config" => Bean de configuration spring. Remplace les anciens fichiers "bean.xml" du temps de spring < v3. Archetype Spring

### Focus techniques

#### Types de données gérés en interne
La grand règle pour l'extraction des données et la préparation pour l'index est d'utiliser uniquement du string. Les données sont simplement "inlinées" sous la forme suivante : 

    COLUMN_NAME=T/B64_ENCODED_VALUE,autres colonnes...

Avec T le type "simplifié" parmi S/O/B. Toutes les valeurs sont inlinés en une seule chaine unique, séparées par une virgule, avec le nom de la colonne en majuscule repris pour chaque valeur. Les colonnes sont ordonnées par leur ordre naturel. Les valeurs sont systématiquement converties en BASE64. Le traitement est globalement traité par les méthodes utilitaires de **fr.uem.efluid.utils.ManagedDiffUtils**

**Pourquoi ce formalisme ?** 
En effet on peut observer que ce n'est par exemple pas le plus compact. Mais il y a des avantages : 
* Le BASE64 implique par exemple environ +25% de taille de données. Mais tel qu'il est défini ici, il permet de simplifier l'opération de comparaison d'une valeur à l'autre, en traitant tous les changements dans la table (valeurs et définition). Le poids n'est pas critique au vu de l'usage attendu (on est sur des données qui font au grand maximum 2Gb en tout, et plutôt des 10Mb en usage courant). Hasher ces valeurs est très simple. La compression (utile pour l'export / import) est optimale). Seuls des caractères ANSI sont utilisés, on peut coder cette chaine sur des charactères de 8bit.
* Si les noms de colonnes sont repris systématiquement et alourdissent ces données, retourner cette valeur pour obtenir un SQL d'application de mise à jour est aisé (on a immédiatement les colonnes et B64 est reversible). De plus le résultat inline reste limité aux caractères ANSI et est hautement compressable pour les exports / imports
* La comparaison peut être faite directement par string. Au vu du volume de données attendu, la comparaison par Hash n'aurait pas d'impact positif, mais c'est à tester.

**Pourquoi cette limitation à trois types S/O/B ?**
Dans les faits, on a 2 types de transpositions à gérer pour chaque données indéxées :
* Lors de l'extraction, on veut obtenir les données dans un format gérable dans l'index. Pour cela, TOUT EST CONVERTI EN STRING. 
* Lors de la réapplication des données, c'est le cas d'usage lié à la génération du SQL de sorti des données qui rentre en compte. Et en SQL il y a en générale 3 possibilités : les chaines de caractères et assimilés, de même que les dates / timestamp (tant que le format est embarqué) sont données avec des caractères "d'identification de début et de fin", soit "'". Les nombres, binaires et booléen sont donnée sans ces caractères, et les CLOB et BLOB sont injectés autrement (suivant les BDD). Les types S/O/B représentent ces trois cas, et sont portés par l'enum **fr.uem.efluid.model.metas.ColumnType**
  
**Pour donner un exemple plus concret de données inlinées, soit une table avec comme colonnes :**
* KEY : PK bigint
* VALUE : Varchar(x)
* VALUE_OTHER : Varchar(x) 
* Something : bigint
* when : timestamp

**L'inline donne le résultat suivant :**

    VALUE=S/QSFQOI42144,VALUE_OTHER=S/JR19RJ19021RK=,SOMETHING=O/14201FIOI==,WHEN=O/090FJZ0F0FI0KF
    
