# PARAMETHOR

Application dédiée à l'identification, au packaging et au déploiement de paramètrage pour une instance Efluid.

## Utilisation de l'application



## Build

### Version Standalone docker

Une configuration complete "standalone" est intégrée. Elle intègre dans un seul container l'application (fat-jar) et une instance postgres complète pour le fonctionnement et la gestion. La configuration peut être montée depuis un volume.

Aucun autre pré-requis que la présence de docker sur le poste de build n'est nécessaire. Le build est traité par un script, où le build java (maven) est réalisé directement dans un container. Pour windows toutes les commandes sont données avec Powershell.

**Pour builder la version standalone**

Pour utiliser le build sur un poste de dev windows, par exemple pour la version avec postgres embarqué : 

    ## être dans le dossier racine du projet
    cd GestionParamEfluid
    ## lancer le script de build depuis le dossier racine
    ./param-app/src/docker/build-desktop/standalone-with-postgres/build.ps1

Pour builder une version avec H2 sur un serveur efluid :

    ## être dans le dossier racine du projet
    cd GestionParamEfluid
    ## lancer le script de build depuis le dossier racine
    ./param-app/src/docker/build-serv-efluid/standalone-with-h2/build.sh

L'instance est déployée dans le répo local docker sous le nom **paramethor**:*latest*

**Pour démarrer la version standalone**

Sous linux / windows, par exemple pour la version avec h2 : 

    docker run -it --rm -p 8080:8080 paramethor:latest-h2

Pour utiliser un fichier de configuration spécifique, le monter sous *"/cfg/application.yml"*. Par exemple : 

Sous linux :

    docker run -it --rm -p 8080:8080 -v $pwd/param-app/src/main/resources/config/application.yml:/cfg/application.yml paramethor:latest-h2

Ou sous windows : 

    docker run -it --rm -p 8080:8080 -v ${pwd}\param-app\src\main\resources\config\application.yml:/cfg/application.yml paramethor:latest-h2

Les BDD sont initialisées, puis l'application démarre. Elle est ensuite accessible sur [http://localhost:8080](http://localhost:8080) Elle démarre en mode Wizzard avec en BDD gérée par défault l'instance local PGSQL

Il existe 2 variantes à ce stade pour la version standalone :

* paramethor:latest-h2 : avec BDD H2 embarquée
* paramethor:latest-pgsql : avec BDD Postgres complète embarquée dans le même container

### Version struff

La version struff n'est pas à jour. Ne pas l'utiliser pour l'instant

## Utilisation en développement

### Quickstart avec Maven
Pour démarrer sans rien installer, juste à partir du projet cloné, en ayant maven sur son poste, et les bases de données nécessaires (voir plus loin), utiliser : 

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

    docker run --name oracle -d -p 49161:1521 sath89/oracle-12c

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

### Genération de dictionnaire à partir de l'API
Une API permettant de spécifier un dictionnaire complet (table, domaines et liens) est fournie dans le module **param-api**

Voici un exemple de mise en oeuvre : 
    
    @GestionDuMateriel
    @ParameterTable(name = "Categorie", tableName = "TCATEGORY")
    public class CategorieDeMateriel {
       
       private Long id;
       
       @ParameterValue("NAME")
       private String name;
       
       @ParameterKey("CODE")
       private String code;
       
       // ...
    }

L'API est utilisée par un générateur dédié spécifié dans le module **param-generator**. Celui ci est avant tout un plugin maven, mis en oeuvre avec la configuration suivante : 

    <plugin>
       <groupId>${project.groupId}</groupId>
       <artifactId>param-generator</artifactId>
       <executions>
          <execution>
             <id>generate</id>
             <phase>generate-sources</phase>
             <goals>
             	   <goal>generate</goal>
             </goals>
          </execution>
       </executions>
       <configuration>
          <destinationFileDesignation>generated-dictionary</destinationFileDesignation>
          <destinationFolder>${project.basedir}/target</destinationFolder>
          <protectColumn>true</protectColumn>
          <sourcePackage>fr.uem.efluid.sample</sourcePackage>
          <uploadToServer>true</uploadToServer>
          <uploadEntryPointUri>http://127.0.0.1:8080/rest/v1/dictionary</uploadEntryPointUri>
       </configuration>
    </plugin>

Ce générateur construit un fichier .par d'export / import de dictionnaire, immédiatement importable dans une instance de l'application. Il est également capable de l'uploader directement sur une instance active de l'application.

Les propriétés de configuration sont : 
* **destinationFileDesignation** : identifiant du nom de l'archive .par. Si égale à "auto", alors un uuid aléatoire est utilisé.
* **destinationFolder** : Emplacement de sortie du fichier .par généré. Par défaut "${project.basedir}/target"
* **protectColumn** : Indique si le format de colonne de la BDD utilisée doit être protégé (identique option équivalente de l'application)
* **sourcePackage** : Racine du package java à parser pour la recherche du modèle du dictionnaire
* **uploadToServer** : Si true, va uploader le .par dans une instance de l'application. Par défaut false
* **uploadEntryPointUri** : Url du point d'entrée du service REST "dictionnaire" de l'application où le .par sera uploadé. Exemple : "http://127.0.0.1:8080/rest/v1/dictionary"

Un projet exemple est fourni : **param-generator-example**, avec un modèle complet. Un script SQL (oracle) d'initialisation des tables correspondantes est fourni dans src/database

### Services REST
Une API de services **REST** est intégrée. 

**Elle permet** :
* De créer / mettre à jour le dictionnaire par import de .par (utilisé pour le système de génération avec l'API java)
* De lancer / suivre / annuler / valider un diff. Il devient donc possible de piloter la préparation de lots directement depuis une application tiers (par exemple depuis Efluid directement)

A ce stade aucune sécurité n'est intégrée, les services REST sont accessibles à tous. Un système à base de d'API token sera mis en place.

Depuis le bandeau haut de l'application, cliquer le bouton "API REST" pour accéder à une interface de navigation / test de l'API (format Swagger + swagger-ui v2).

## Conception / Principes
CF support de présentation.

### Stack technique
Spring-boot avec :
* **Spring MVC + Thymeleaf** : Contrôleurs WEB avec templates Thymeleaf + Contrôleurs Rest pour services d'intégration
* **Spring core** : IoC, core contexte
* **(Spring Security)** - accès sécurité très simplifié, à voir
* **Spring data JPA** : Manipulation BDD core, avec JPA, via répository spécifiés seulement sous la forme d'interfaces java
* **Spring JDBC avec Spring Transaction** : Pour les manipulations de la BDD managed, via JDBC directement

Utilise au maximum les conventions Spring-boot pour éliminer tout code technique. 

Pour la sécurité, chaque user a un "token" technique privé disponible : il suffit de le préciser dans l'url de l'appel REST.

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

### Gestion de la sécurité
Utilisation de [pac4j](http://www.pac4j.org/) "seul" (sans spring-security). La configuration technique des composants de sécurité est faite dans *fr.uem.efluid.config.SecurityConfig*

Deux *clients* pac4j avec 2 chaines d'authentification / interception :
* Les urls "/ui/*" (écrans de l'application) sont sécurisés avec une authentification "web" classique (login), avec compte en session.
* Les urls "/rest/*" (services rest) sont sécurisés avec une récupération de token privé propre à chaque user.

Attention, ce n'est pas supposé être "web-proof" (mais l'application n'est pas supposée l'être du tout : on parle là d'un outil d'administration de données directement en BDD, un peu comme l'est Oracle SQL Developer)

Il n'y a pas de rôle géré (pour l'ajouter : créer données en BDD et les utiliser dans les checks réalisés dans fr.uem.efluid.security.AllAuthorizer). Le mot de passe est encodé en SHA-256 (hash simple, pouvant être amélioré - ajout de salt par exemple - : géré dans fr.uem.efluid.security.ShaPasswordEncoder).

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
    
### Requêtes utiles 
**Consulter l'index**

    select 
       idx.id, 
       concat(p.key_name,'=',idx.key_value) as key, 
       p.table_name, 
       p.where_clause as where, 
       idx.action, 
       idx.payload, 
       c.original_user_email as commit_by 
    from index idx 
    inner join dictionary p on p.uuid = idx.dictionary_entry_uuid 
    inner join commit c on c.uuid = idx.commit_uuid
    order by table_name, id
    
**Supprimer un commit pour retester un import / merge**

Penser à vérifier d'abord les commits présents

	select * from commit

Puis il est possible de supprimer le commit et les données associées à partir d'un critère sur le "comment" (ici pour un merge) :

	delete from index where commit_uuid = (select uuid from commit where comment like '###%');
	delete from commit_merge_sources where commit_uuid = (select uuid from commit where comment like '###%');
	delete from lobs where commit_uuid = (select uuid from commit where comment like '###%');
	delete from commit where uuid = (select uuid from commit where comment like '###%');