# Gestion du paramètrage Efluid

Prototype d'application dédiée à l'identification, au packaging et au déploiement de paramètrage pour une instance Efluid.

## Avancement général

*Mise à jour au 03/02/2018*

**Maquette statique**
* Dernière version : 03/02/2018. Modification de la gestion des entrées dans le dictionnaire : se fait sur un autre écran de saisie à partir de la liste

**Points techniques**
* Mise en place général : projet actif, stacks en place. BDD Postgres core fonctionnelle
* Modèle core : en place, avec repos
* Utilisation BDD managed : Avec JDBCTemplate. Dev fait sur lecture données bruts. Partiellement testé (metadata globalement OK mais infos PK pas encore correctement remontées)
* Preparation au diff : "regénération" des données préues à partir de l'index en place, testé.
* Gestion du diff : diff de base en place, testé
* Un benchmark synthétique des fonctions principale en place. Sera amélioré plus tard
* Init de données de démo actif. La BDD et des données de tests sont créées automatiquement au démarrage
* Build : maven OK. Tests faits pour avoir un CI dédié (drone.io?)

**Intégration maquette**
* Mise en place d'un layout pour simpifier les templates Thymeleaf
* index ("/") : page dynamique mais données en dur. Menu et authentification pas géré. 
* édition des domaines fonctionnels ("/domains"): globalement implémenté. 
* édition du dictionnaire ("/dictionary"): implémentation en cours. Liste dyn, édition en cours de mise en place sur 2 écrans cette fois. La partie "selection de la table" est en place.
* autres écrans : pas dynamisés / intégrés avec thymeleaf

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

    docker run --name test-postgres -e POSTGRES_PASSWORD=test -p 5432:5432 -d postgres

Puis création de 2 databases : manager et demo. Création d'un user "user"/"user" owner de ces DB

#### Schéma demo
Pour intiliser l'instance demo : Elle est utilisée pour représenter une application dont la gestion du paramètrage serait pilotée avec l'application.

     -- DROP TABLE "TTYPEMATERIEL";
     -- DROP TABLE "TCATEGORYMATERIEL";
     
     CREATE TABLE "TCATEGORYMATERIEL"
     (
         "ID" bigint NOT NULL,
         "NOM" character varying(256)[] COLLATE pg_catalog."default" NOT NULL,
         "DETAIL" character varying(256)[] COLLATE pg_catalog."default" NOT NULL,
         CONSTRAINT "TCATEGORYMATERIEL_pkey" PRIMARY KEY ("ID")
     )
     WITH (
         OIDS = FALSE
     )
     TABLESPACE pg_default;
     
     ALTER TABLE "TCATEGORYMATERIEL"
         OWNER to "user";
     
     
     CREATE TABLE "TTYPEMATERIEL"
     (
         "ID" bigint NOT NULL,
         "TYPE" character varying(256)[] COLLATE pg_catalog."default" NOT NULL,
         "SERIE" character varying(256)[] COLLATE pg_catalog."default",
         "CATID" bigint NOT NULL,
         CONSTRAINT "TTYPEMATERIEL_pkey" PRIMARY KEY ("ID"),
         CONSTRAINT "CAT_TYPE" FOREIGN KEY ("CATID")
             REFERENCES "TCATEGORYMATERIEL" ("ID") MATCH SIMPLE
             ON UPDATE NO ACTION
             ON DELETE NO ACTION
     )
     WITH (
         OIDS = FALSE
     )
     TABLESPACE pg_default;
     
     ALTER TABLE "TTYPEMATERIEL"
         OWNER to "user";
         
Ce script sera complété avec un modèle plus complexe plus tard. Des scripts d'initialisation de données seront également fournis.

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

