<img src="efluid-datagate-app/src/main/resources/static/logo.png?raw=true" width="300"/>

Efluid Datagate - Advanced database value management
=====================

[![Build Status](https://build.elecomte.com/api/badges/datagate-update/efluidDataGate/status.svg?ref=refs/heads/develop)](https://build.elecomte.com/datagate-update/efluidDataGate)

Outil dédié à l'identification, au packaging et au déploiement de modifications dans une base de données, en s'appuyant sur les principes de `git`. 

Projet mis en place initialement pour la gestion et l'évolution du paramètrage pour une instance de l'application [Efluid](https://www.efluid.com/)

## Documentation

La documentation générale est disponible ici : [accueil doc](docs/README.md) (WIP)


## Build

### Build standard 

La gestion du build est portée par maven.

Pour packager l'application, lancer : 
```
mvn package
```

### Build auto drone

Un pipeline drone est spécifié à la racine

### Version Standalone docker

Une configuration complete "standalone" est intégrée. Elle intègre dans un seul container l'application (fat-jar) et une instance postgres complète pour le fonctionnement et la gestion. La configuration peut être montée depuis un volume.

Aucun autre pré-requis que la présence de docker sur le poste de build n'est nécessaire. Le build est traité par un script, où le build java (maven) est réalisé directement dans un container. Pour windows toutes les commandes sont données avec Powershell.

**Pour builder la version standalone**

Pour utiliser le build sur un poste de dev windows, par exemple pour la version avec postgres embarqué : 
```
# être dans le dossier racine du projet
cd efluidDataGate

# lancer le script de build depuis le dossier racine
./efluid-datagate-app/src/docker/build-desktop/standalone-with-postgres/build.ps1
```

Pour builder une version avec H2 sur un serveur efluid :
```
# être dans le dossier racine du projet
cd efluidDataGate

# lancer le script de build depuis le dossier racine
./efluid-datagate-app/src/docker/build-serv-efluid/standalone-with-h2/build.sh
```

L'instance est déployée dans le répo local docker sous le nom **efluid-datagate**:*${PROJECT_VERSION}*

**Pour démarrer la version standalone server avec les scripts fournis**

S'assurer au préalable qu'un dossier est prévu pour stocker les éléments logs et cfg de efluidDataGate, comme `/opt/server/efluidDataGate`

Après build, faire : 
``` 
cp ./efluid-datagate-app/src/docker/start-efluidDataGate.sh /opt/server/efluidDataGate/
```

Copier éventuellement la configuration désirée dans `/opt/server/efluidDataGate/dest/cfg/application.yml` et `/opt/server/efluidDataGate/src/cfg/application.yml` et lancer avec :
```
# Exemple de création / lancement d'une instance "src" sur le port 8080
sudo /opt/server/efluidDataGate/start-efluidDataGate.sh src 8080

# Exemple de création / lancement d'une instance "dest" sur le port 808&
sudo /opt/server/efluidDataGate/start-efluidDataGate.sh dest 8081
```

**Pour démarrer la version standalone - A LA MAIN**

Sous linux / windows, par exemple pour la version avec h2 : 
```
docker run -it --rm -p 8080:8080 efluid-datagate:latest-h2
```

Pour utiliser un fichier de configuration spécifique, le monter sous *"/cfg/application.yml"*. Par exemple : 

Sous linux :
```
docker run -it --rm -p 8080:8080 -v $pwd/efluid-datagate-app/src/main/resources/config/application.yml:/cfg/application.yml efluid-datagate:latest-h2
```

Ou sous windows : 
```
docker run -it --rm -p 8080:8080 -v ${pwd}\efluid-datagate-app\src\main\resources\config\application.yml:/cfg/application.yml efluid-datagate:latest-h2
```

Les BDD sont initialisées, puis l'application démarre. Elle est ensuite accessible sur [http://localhost:8080](http://localhost:8080) Elle démarre en mode Wizzard avec en BDD gérée par défault l'instance local PGSQL

Il existe 2 variantes à ce stade pour la version standalone :

* efluid-datagate:latest-h2 : avec BDD H2 embarquée
* efluid-datagate:latest-pgsql : avec BDD Postgres complète embarquée dans le même container

### Version struff

La version struff n'est pas à jour. Ne pas l'utiliser pour l'instant


## Utilisation en développement

### Grandes lignes

Prérequis :
* JDK11 (openjdk)
* Maven 3.x
* IDE (intellij recommandé)

Idéalement docker desktop pour monter des BDD locales facilement

### Disponibilité de bases de données de développement

**2 Bases de données sont nécessaires :**
* Une BDD pour le fonctionnement de l'application: "BDD de management"
* Une BDD cible des opérations de migrations: "BDD managée" = "BDD source"

Pour permettre un démarrage et des tests adaptés pendant le développement, il est recommandé de mettre à disposition 2 bases dédiées pour chaque développeur

La BDD de référence est Oracle (postgres et H2 sont aussi supportés). 

> Nous donnons ici des procédures de mise en place de BDD locales pour Postgres, Oracle XE 12c et Oracle XE 18c

#### Instance Postgres
Démarrage avec docker :
```
docker run --name test-postgres -e POSTGRES_PASSWORD=test -p 5432:5432 -v /opt/server/postgresql/data:/var/lib/postgresql/data -d postgres
```

Puis création de 2 databases : `MANAGER` et `DEMO` + Création d'un user "user"/"user" owner de ces 2 DB

> Tester les connexions locales avec `MANAGER` et `DEMO`

#### Instance Oracle XE 12c
Oracle XE peut être utilisé pour représenter l'application demo. Une version 12c est disponible sur docker hub.

Démarrage avec docker :
```
docker run --name oracle -d -p 49161:1521 sath89/oracle-12c
```

Se connecter ensuite à l'instance avec SQL Developer avec les paramètres : 
* hostname: ...suivant installation...
* port: 49161
* sid: xe
* username: system
* password: oracle (par defaut)

*Enfin, créer les comptes utilisateurs suivants* :

* MANAGER (BDD de management), pwd MANAGER, Tablespace USERS, accorder tous les droits
* DEMO (utilisé pour avoir une instance DEMO "simulée" locale), pwd DEMO, Tablespace USERS, accorder tous les droits

> Tester les connexions locales avec `MANAGER` et `DEMO`

#### Instance Oracle XE 18c

Pour démarrer une instance Docker de Oracle 18c il faut cette fois d'abord **construire** l'image docker à partir d'outils fournis par Oracle directement, en suivant **la procédure ci dessous**

> Cette instance XE utilisera des droits par défaut, des mots de passes simples, elle n'est pas du tout sécurisée et cette procédure ne devrait pas être appliquée sur un environnement "fonctionnel"

##### 1/ Construction de l'image docker Oracle XE 18

Commencer par créer une XE 18 avec docker (testé sur Windows, doit fonctionner sur Windows ou linux sans changement). 

Depuis fin 2018 des dockerfiles standards sont disponibles dans un projet Oracle Github, et proposent tout le nécessaire :

* Suivre https://github.com/oracle/docker-images/blob/master/OracleDatabase/SingleInstance/README.md
* Cloner le projet `docker-images` localement et y accéder
* Télécharger https://www.oracle.com/technetwork/database/database-technologies/express-edition/downloads/index.html (build x64 linux)
* Installer le rpm dans `./docker-images/OracleDatabase/SingleInstance/dockerfiles/18.4.0`
* Lancer : `./docker-images/OracleDatabase/SingleInstance/dockerfiles/buildDockerImage.sh -v 18.4.0 -x -i`

> L'image est dispo (localement) en tant que `oracle/database:18.4.0-xe`

##### 2/ Démarrage container

*Démarrer l'instance docker* :

```
docker run --name oracle-18 -d -p 49121:1521 -e ORACLE_SID=xe -e ORACLE_PWD=dba -e ORACLE_CHARACTERSET=WE8ISO8859P15 oracle/database:18.4.0-xe
```

La BDD est installée au démarrage. A priori les erreurs rencontrés n'ont pas d'importance

> Cette instance pourra être relancée avec `docker start oracle-18`

##### 3/ Création des utilisateurs

La création des comptes nécessite de se connecter à l'instance créée en tant que sysdba avec SQLDeveloper. 

*Pour cela, utiliser comme paramètres* : 

* port `49121`
* compte `sys` / `dba`
* Rôle `SYSDBA`

*Puis avant de démarrer la création de compte, lancer* :
```
alter session set "_oracle_script"=true;
```

*Enfin, créer les comptes utilisateurs suivants* :

* MANAGER (BDD de management), pwd MANAGER, Tablespace USERS, accorder tous les droits
* DEMO (utilisé pour avoir une instance DEMO "simulée" locale), pwd DEMO, Tablespace USERS, accorder tous les droits

> Tester les connexions locales avec `MANAGER` et `DEMO`

##### 4/ Création des données

En utilisant les connexions MANAGER et DEMO configurées, installer les données de test nécessaires.

* Pour DEMO, utiliser le script `examples/install_init_oracle_demo.sql` : un ensemble de données de test pour une BDD gérée est créé
* Pour MANAGER, utiliser le script `examples/install_init_oracle_manager.sql` : Le schéma de fonctionnement de l'application est créé (pas besoin d'utiliser le DDL HBM comme cela)

### Création de la configuration spécifique du développeur

Créer un fichier application-dev.yml sur la base de cet exemple (ici pour utiliser des instances oracles locales montées sur `localhost:49121` avec les PWD par défaut): 
```
datagate-efluid:

    managed-datasource:
        driver-class-name: oracle.jdbc.OracleDriver
        url: jdbc:oracle:thin:@localhost:49121:xe
        username: DEMO
        password: DEMO
        meta:
            filter-schema: DEMO

    display:
        diff-page-size: 10

    security:
        salt: 12345678901234567890123456789012

    details:
        instance-name: REFERENCE-EFLUID-18.4

    model-identifier:
        enabled: true
        class-name: fr.uem.efluid.tools.EfluidDatabaseIdentifier
        show-sql: true

    web-options:
        enable-custom-h2-console: false

## TECH FEATURES CUSTOM
spring:
    profiles:
        active: prod

    datasource:
        driver-class-name: oracle.jdbc.OracleDriver
        url: jdbc:oracle:thin:@localhost:49121:xe
        username: MANAGER
        password: MANAGER

    jpa:
        show-sql: true
        hibernate:
            ddl-auto: none

    flyway:
        enabled: false

## WEB SERVER CONFIG
server:
    port: 8085
```

> Les fichiers `*.yml` sont gitignorés dans le dossier resources/config de l'app. Il est possible d'y gérer sa conf personnelle

### Démarrage depuis un IDE
Pour démarrer depuis un IDE, lancer la classe exécutable `fr.uem.efluid.Application`

Ajouter les options de démarrage suivantes :
```
--spring.config.location=classpath:/application.yml,classpath:/config/application-dev.yml
```

> Il peut être nécessaire d'ajuster les options de JVM en fonction de votre environnement



## Aide au développement / maintenance

### Utilisation des tests automatisés
Le comportement des fonctions clés de l'application est validé par différents tests automatisés, de types tests unitaires et tests d'intégration.
Ces tests sont exécutés automatiquement lors du build avec Maven. Ils peuvent être démarrés manuellement depuis l'IDE directement également

A noter : les tests d'intégrations utilisent une BDD embarquée H2, droppée après chaque exécution (et rollbackée après chaque cas de test). Une console web est accessible pour consulter les données de la BDD H2 utilisée : il suffit d'ajouter un point d'arrêt sur un test dont on souhaite suivre le comportement au niveau BDD, puis d'accéder à la console à l'adresse [http://localhost:8082](http://localhost:8082). Dans l'interface de connexion, utiliser les paramètres de connexion suivants : 
* **url**: jdbc:h2:~\h2;
* **username**: sa
* **password**: *-- laisser vide --*

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

## License 

Apache public License 2.0
