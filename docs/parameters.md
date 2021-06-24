# Paramètrage de Datagate

## Principes de la configuration

### Modèle de configuration de spring-boot
Datagate est une application développée avec `spring-boot`. 

**Les principes de configuration de `spring-boot` peuvent donc s'appliquer** : 
* La configuration est **hierarchisée**. Une version "racine" de la configuration est embarquée avec le code de l'application, et définie tous les réglages par défaut. Il existe même au "dessus" de cette
* Un fichier de **"surcharge"** peut être spécifié pour donner des valeurs différentes sur certains paramètres, lorsque les valeurs par défaut ne sont pas adaptées
* D'autres fichiers de surcharge peuvent s'enchainer de la mnême manière
* Les paramètres peuvent être également spécifiés par **variables d'environnement**, **arguments** de lignes de commandes ...

> Pour en savoir plus sur la gestion du paramètrage avec une application `spring-boot` regarder en particuliers https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config

### Formes possibles de paramètrages

Comme pour toute application `spring-boot` les paramètres peuvent être définis sous différentes formes :
* dans un fichier yaml. Les propriétés sont données au format kebab-case
* Dans un fichier properties, kebab-case également
* En variable d'environnement, format snake-case majuscule
* En argument au lancement de l'application, format kebab-case
* Programmatiquement, format camel-case
* Pour certains paramètres, une modification "à chaud" de certaines features via une API rest est également possible (voir chapitre dédié plus bas)

Par exemple la propriété de paramètrage suivante dans un fichier Yaml : 
```
datagate-efluid:
    managed-datasource:
        driver-class-name: oracle.jdbc.OracleDriver
```
peut être définie comme ceci dans un fichier properties :
```
datagate-efluid.managed-datasource.driver-class-name=oracle.jdbc.OracleDriver
```
en argument au lancement du programme : 
```
java -jar ... --datagate-efluid.managed-datasource.driver-class-name=oracle.jdbc.OracleDriver
```
ou encore en variable d'environnement : 
```
export DATAGATE_EFLUID_MANAGED_DATASOURCE_DRIVER_CLASS_NAME=oracle.jdbc.OracleDriver
```

> Pour une présentation des formats de cases, voir https://medium.com/better-programming/string-case-styles-camel-pascal-snake-and-kebab-case-981407998841

### Utilisation d'alias

Il est possible dans un fichier de configuration de l'application d'utiliser des alias pour référencer :
* D'autres paramètres du même fichier
* Des alias simplifiés spécifiques, par exemple pour utiliser des variables d'environnement

La syntaxe utilisée est celle des `spring-el` : `${property}`

Par exemple dans un fichier on peut spécifier une valeur et la réutiliser ailleurs (ici yaml): 
```
spring:
    datasource:
        url: jdbc:postgresql://database:1234
        driver-class-name: org.postgresql.Driver
        username: user
        password: pwd

    jpa:
        open-in-view: false

    flyway:
        enabled: false
        url: ${spring.datasource.url}
        user: ${spring.datasource.username}
        password: ${spring.datasource.password}
```

On peut également créer des alias pour simplifier l'utilisation d'argument ou de variable d'environnement :
```
spring:
    datasource:
        url: ${DB_URL}
        driver-class-name: org.postgresql.Driver
        username: ${DB_USER}
        password: ${DB_PASSWORD}
```
ce qui permet d'utiliser ici directement les variables d'environnement `$DB_URL`, `$DB_USER` et `$DB_PASSWORD`

## Configuration en place pour Datagate 

### Configuration racine

La configuration racine donne le paramètrage par défaut. Toutes les valeurs gérées par l'application sont prédéfinies.

Les paramètres gérés sont :

* `datagate-efluid.managed-datasource.driver-class-name`: Driver JDBC à utiliser pour la connexion à la BDD gérée
* `datagate-efluid.managed-datasource.url`: URL de la BDD gérée
* `datagate-efluid.managed-datasource.username`: Login pour la connexion à la BDD gérée
* `datagate-efluid.managed-datasource.password`: MDP pour la connexion à la BDD gérée
* `datagate-efluid.managed-datasource.connection-test-query`: Requête de test de connexion à la BDD gérée
* `datagate-efluid.managed-datasource.minimum-idle`: Taille minimale du pool de connexion à la BDD gérée
* `datagate-efluid.managed-datasource.max-pool-size`: Taille maxi du pool de connexion à la BDD gérée
* `datagate-efluid.managed-datasource.timeout`: TTL des connexions à la BDD gérée
* `datagate-efluid.managed-datasource.meta.filter-schema`: Schéma de la BDD gérée, utilisé pour la recherche des métadonnées. Peut être différent du login de connexion
* `datagate-efluid.managed-datasource.meta.fixed-cached` : Dépréciée, non utilisé
* `datagate-efluid.managed-datasource.meta.search-fk-type`: Type de recherche des FK à partir des métadonnées. Peut être `oracle-by-name` (recherche par nom de colonne, optimisé pour oracle), `oracle-by-details` (recherche des FK spécifiées dans les métadonnées, optimisé pour oracle) ou `disabled` (désactivé). Pour les BDD autres que Oracle une recherche pure JDBC est automatiquement utilisée et ce paramètre est ignoré
* `datagate-efluid.managed-datasource.meta.preload`: true pour précharger les métadonnées au démarrage. En cas de changement il faut relancer l'application
* `datagate-efluid.managed-datasource.query.table-names-protected`: true si des doubles quotes doivent être spécifiées pour les noms de tables dans les requêtes sur la BDD gérée
* `datagate-efluid.managed-datasource.query.column-names-protected`: true si des doubles quotes doivent être spécifiées pour les noms de colonnes dans les requêtes sur la BDD gérée
* `datagate-efluid.managed-datasource.query.database-date-format`: Format de date standard de la BDD gérée
* `datagate-efluid.managed-datasource.query.join-on-nullable-keys`: true si la BDD gérée peut avoir des FK "fonctionnelles" utilisant `null` comme valeur autorisée
* `datagate-efluid.managed-datasource.value.keep-empty`: true si les valeurs `null` doivent être spécifiées dans les payloads de l'index. Alourdi le payload
* `datagate-efluid.managed-updates.check-update-missing-ids`: true si un recherche des ids manquants est réalisée avant l'application d'un update des données de la BDD gérée suite à un import de lot. Les valeurs manquantes sont ignorées
* `datagate-efluid.managed-updates.check-delete-missing-ids`: true si un recherche des ids manquants est réalisée avant l'application d'un delete des données de la BDD gérée suite à un import de lot. Les valeurs manquantes sont ignorées
* `datagate-efluid.managed-updates.output-failed-query-set`: true pour tracer les erreurs de mise à jour de données
* `datagate-efluid.managed-updates.output-failed-query-set-file`: nom d'un fichier où seront sortis les erreurs de mise à jour de données si elles sont tracées
* `datagate-efluid.display.details-index-max`: nombre de ligne de diff maxi avant d'utiliser une vue simplifier dans l'écran détail d'un lot existant
* `datagate-efluid.display.combine-similar-diff-after`: nombre de changement similaire nécessaires avant de les combiner dans l'écran diff sous la forme d'un seul changement
* `datagate-efluid.display.history-page-size`: Nombre d'items par page pour la pagination de l'historique des changements enregistrés
* `datagate-efluid.display.diff-page-size`: Nombre d'items par page pour la pagination du diff d'une préparation d'un lot
* `datagate-efluid.display.details-page-size`: Nombre d'items par page pour la pagination du diff présenté en consultant le contenu d'un lot existant
* `datagate-efluid.display.test-row-max-size`: Nombre d'items par page pour la pagination de la vue "test" de donnée dans la spécification d'une table de paramètrage
* `datagate-efluid.web-options.enable-custom-h2-console`: true pour activer la console h2 (quand une BDD H2 est utilisée comme BDD gérée, c'est à dire uniquement en mode démo)
* `datagate-efluid.dictionary.select-pk-as-default-keys`: true si les PK des tables peuvent être pré-selectionnées comme clés dans l'édition d'une table de paramètrage
* `datagate-efluid.versions.use-model-id-as-version`: true si le model identifié de la BDD gérée est utilisé comme nom de version
* `datagate-efluid.security.salt`: hash salt pour la génération des tokens d'appel API
* `datagate-efluid.security.accounting`: Mode de gestion des comptes utilisateurs. Peut être `DATABASE`, `LDAP_AUTH` ou `LDAP_FULL` (seuls `DATABASE`, `LDAP_AUTH` sont supportés à ce stade)
* `datagate-efluid.security.ldap.user-search-base`: (uniquement pour l'accounting `LDAP_AUTH` ou `LDAP_FULL`) DN (sans intégrer le base dn spécifié sur spring) ou rechercher des comptes utilisateurs. Exemple : `ou=Personne,o=entity`
* `datagate-efluid.security.ldap.username-attribute`: (uniquement pour l'accounting `LDAP_AUTH` ou `LDAP_FULL`) Attribut ldap à utiliser comme login. Exemple : `uid`
* `datagate-efluid.security.ldap.mail-attribute`: (uniquement pour l'accounting `LDAP_AUTH` ou `LDAP_FULL`) Attribut ldap à utiliser comme email utilisateur. Exemple : `mail`
* `datagate-efluid.security.ldap.use-auth-binding`: (uniquement pour l'accounting `LDAP_FULL`) non utilisé à ce stade
* `datagate-efluid.details.version`: Version de datagate
* `datagate-efluid.extractor.show-sql`: true si les requêtes d'extraction depuis la BDD gérée doivent être loggées. A ne pas confondre avec l'output des requêtes JPA (l'extraction n'utilise pas JPA)
* `datagate-efluid.extractor.use-label-for-col-name`: true si le label des colonnes est utilisé pour aliaser les valeurs dans les requêtes d'extraction
* `datagate-efluid.attachments.enable-sql-execute`: true si l'exécution des fichiers SQL spécifiés en PJ de lot est activé
* `datagate-efluid.attachments.enable-display`: true si la consultation des fichiers spécifiés en PJ de lot est activé
* `datagate-efluid.imports.check-model-version`: true si l'import d'un lot est conditionné sur la version attachée (vérifie uniquement que la version est présente)
* `datagate-efluid.imports.check-dictionary-compatibility`: true si l'import d'un lot est conditionné par la présence d'un dictionnaire compatible (vérifie en profondeur le contenu du lot et le paramètrage du dictionnaire en place pour valider que les données sont compatibles)
* `datagate-efluid.imports.check-missing-ref-commits`: true si l'import de lot doit remonter une erreur si un lot "référencé" (lot non exporté précédent le ou les lots exportés, en cas d'export partiel ou de type "cherry-pick") n'a pas déjà été importé localement. False par défaut (pour permettre le vrai cherry-pick)
* `datagate-efluid.async-preparation.thread-pool-size`: taille du thread pool pour l'exécution de la préparation de lots
* `datagate-efluid.async-preparation.timeout-seconds`: Timeout pour un lancement de prépraration de lots avant son interuption automatique
* `datagate-efluid.model-identifier.enabled`: true si la gestion d'un model id (identifiant de BDD gérée extrait à partir d'une table de la BDD) est activée
* `datagate-efluid.model-identifier.class-name`: Classe spécifique de recherche d'un model id 
* `datagate-efluid.model-identifier.show-sql`: true si les requêtes de recherche de model id sont loggées
* `datagate-efluid.merge.rule-file`: emplacement du fichier de définition des règles de merge pour le nouveau modèle de merge
* `spring.profiles.active` (mappé dans les applications efluid en tant que `application.profile.active`) : choix du profil spring actif (un profil spring est un "flag" d'état de configuration pour activer rapidement certaines comportement en choisissant une valeur donnée). Utilisé pour datagate seulement pour activer le profile spécifique `test` au lieu du standard `prod`, pour activer certains chargement de configuration propres aux tests automatiques. Les profiles `demo` et `minimal` ne sont plus supportés
* `spring.datasource.url`: URL de la BDD de fonctionnement de l'application
* `spring.datasource.driver-class-name`: Driver JDBC de la BDD de fonctionnement de l'application
* `spring.datasource.username`: Login de la BDD de fonctionnement de l'application
* `spring.datasource.password`: MDP de la BDD de fonctionnement de l'application
* `spring.jpa.show-sql` (mappé dans les applications efluid en tant que `install.application.show.sql`) : `true` pour logguer toutes les requêtes de fonctionnement de l'application générées avec jpa (cad toutes les requêtes pour la base de fonctionnement - pour la base managée, activer `datagate-efluid.extractor.show-sql`)
* `spring.ldap.base`: (uniquement pour l'accounting `LDAP_AUTH` ou `LDAP_FULL`) DN de base du référentiel LDAP. Par exemple `dc=company,dc=com`
* `spring.ldap.password`: (uniquement pour l'accounting `LDAP_AUTH` ou `LDAP_FULL`) Mot de passe pour la connexion au référentiel LDAP (nécessaire pour valider les DN des utilisateurs pendant l'authentification)
* `spring.ldap.username`: (uniquement pour l'accounting `LDAP_AUTH` ou `LDAP_FULL`) DN du compte à utiliser pour la connexion au référentiel LDAP. Par exemple `cn=admin,dc=company,dc=com`
* `spring.ldap.urls`: (uniquement pour l'accounting `LDAP_AUTH` ou `LDAP_FULL`) URL du référentiel LDAP. Support ldap et ldaps. Par exemple `ldap://my-server:389`
* `server.contextPath`: context web de l'application
* `server.port`: Port TCP de l'application
* `logging.*` : Configuration des logs standards

> Il existe des paramètres supplémentaires liés à la configuration par défaut fixée par `spring-boot`. Tous les paramètres pouvant être utilisés sont donnés ici https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html

### Configuration surchargeable courante

Pour le développement, les tests, les opérations courantes, certains des paramètres existants dans la configuration datagate sont surchargés, généralement avec un fichier yaml supplémentaire.

Voici un exemple de configuration yaml pour un environnement de développement, avec uniquement les paramètres utiles surchargés : 
```
---
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
        show-sql: false

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
        show-sql: false
        hibernate:
            ddl-auto: none

    flyway:
        enabled: true
        baseline-on-migrate: true
        baseline-version: 3

## WEB SERVER CONFIG
server:
    port: 8085
```

### Configuration spécifique minimale

Par rapport à la configuration surchargeable, les propriétés suivantes sont les plus importantes :

* `datagate-efluid.managed-datasource.driver-class-name`: Exemple : `oracle.jdbc.OracleDriver`
* `datagate-efluid.managed-datasource.url`: Exemple : `jdbc:oracle:thin:@localhost:49121:xe`
* `datagate-efluid.managed-datasource.username`: Exemple : `DEMO`
* `datagate-efluid.managed-datasource.password`: Exemple : `DEMO`
* `datagate-efluid.managed-datasource.meta.filter-schema`: Exemple : `DEMO`
* `spring.datasource.driver-class-name`: Exemple : `oracle.jdbc.OracleDriver`
* `spring.datasource.url`: Exemple : `jdbc:oracle:thin:@localhost:49121:xe`
* `spring.datasource.username`: Exemple : `MANAGER`
* `spring.datasource.password`: Exemple : `MANAGER`
* `server.port`: Exemple : `8085`

> En cas de normalisation des déploiements, il est possible (et recommandé) de spécifier un fichier "commun" avec les propriétés communes, et un spécifique par environnement, ou utiliser des variables d'environnement ou des arguments pour les propriétés absolument spécifiques

### Configuration des logs

Les traces de type "INFO" sont sorties par défaut sur la sortie standard uniquement.

* Seul un paramètrage minimal est défini dans l'`application.yml` embarqué dans l'application.
* Ce paramètrage peut être surchargé. Par exemple pour ajouter une sortie dans un fichier de log `/usr/logs/datagate.log`, le paramètre `logging.file` peut être spécifié, comme ici :
```
java -jar ... --logging.file=/usr/logs/datagate.log"
```
* Pour d'avantage de possibilités, un fichier de configuration supplémentaire `logback.xml` peut être spécifié avec la propriété `logging.config`, en configuration ou en argument au démarrage, comme par exemple ici :
```
java -jar ... --logging.config=file:$log_cfg"
```

> Il est également possible de spécifier directement les niveaux de log ou les appenders grace à d'autres propriétés standards de spring-boot : voir https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging

## Proposition de configuration pour un déploiement

### Déploiement sur un poste de développement

Préparer un fichier classpath:/config/application-local.yml et y reprendre la configuration type donnée plus haut.

Démarrer l'application depuis l'IDE de dev avec les arguments de CMD suivants : 
```
--spring.config.location=classpath:/application.yml,classpath:/config/application-local.yml
```

> Ainsi la configuration par défaut (`classpath:/application.yml`) est chargée, puis la configuration spécifique de dev

### Déploiement dockerisé type intégration

Exemple de dockerfile : 
```
FROM openjdk:11-jre

RUN apt-get update
RUN apt-get -y install dos2unix

EXPOSE 8080
EXPOSE 8087
EXPOSE 8000

ENV remoteXdebug=false
ENV xms=256m
ENV xmx=512m

RUN mkdir /app

COPY efluid-datagate-app/target/efluid-datagate-app-exec.jar /app/efluid-datagate-app-exec.jar
COPY efluid-datagate-app/target/run.sh /app/run.sh

RUN dos2unix /app/run.sh && chmod +x /app/run.sh

VOLUME  ["/tmp", "/logs", "/cfg", "/data"]

CMD ["/app/run.sh"]
```

Avec comme fichier run.sh :
```
#!/bin/bash

## STARTUP SCRIPT FOR APP IN DOCKER IMAGE

## CONFIG FILES
app_cfg="/cfg/application.yml"
log_cfg="/cfg/logback.xml"

## OPTIONS FOR DEBUG
activate_debug_param="-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y"
java_option="-Xmx$xmx -Xms$xms -jar /app/efluid-datagate-app-exec.jar --spring.config.location=classpath:/application.yml,file:$app_cfg --logging.config=file:$log_cfg"

echo "STARTING EFLUID-DATAGATE"

## LAUNCH APP
$JAVA_HOME/bin/java $java_option
```
Sur l'hote d'exécution la configuration est montée dans un volume, et reprend le modèle de configuration minimal.

> S'applique pour un environnement où la configuration peut être "montée" dans le container, à partir du volume `/cfg`

### Déploiement dockerisé orchestré ou k8s 

Pour un déploiement où la spécification de l'environnement est paramètrée avec un orchestrateur, il est recommandé d'utiliser un fichier **commun** minimal utilisant des alias vers les variables de paramètrage "absolument spécifiques".

Exemple de fichier commun, format properties, à embarqué au build de l'image : 
```
datagate-efluid.managed-datasource.driver-class-name=oracle.jdbc.OracleDriver
datagate-efluid.managed-datasource.url=jdbc:oracle:thin:@${MANAGED_DB_LOCATION}
datagate-efluid.managed-datasource.username=${MANAGED_DB_USER}
datagate-efluid.managed-datasource.password=${MANAGED_DB_PWD}
datagate-efluid.managed-datasource.meta.filter-schema=${datagate-efluid.managed-datasource.username}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.datasource.url=jdbc:oracle:thin:@${WORK_DB_LOCATION}
spring.datasource.username=${WORK_DB_USER}
spring.datasource.password=${WORK_DB_PWD}
server.port=8080
logging.config=/cfg/logback.xml
```

Créer également un fichier de configuration de log adapté à l'emplacement `cfg/logback.xml` du container, pour prendre en compte notament la centralisation des logs.

Utiliser alors dans la spécification du container / du POD des variables d'environnement pour définir : 
* MANAGED_DB_LOCATION
* MANAGED_DB_USER
* MANAGED_DB_PWD
* WORK_DB_LOCATION
* WORK_DB_USER
* WORK_DB_PWD

Le container doit voir le port `8080` routé

> D'autres paramètres peuvent s'ajouter, pour la gestion mémoire de la JVM par exemple. Ceux ci sont à spécifier avec d'autres mécanismes propres au déploiement d'une application java dans un environnement containerisé / orchestré. 

## Configuration de "features" modifiable à chaud

Certains paramètrages sont activables également "à chaud" grace à un service REST dédié. Les fonctions correspondantes sont appelées "Features" (dans le cadre du principe de "feature flipping").

Les features existantes sont :
* `CHECK_DICTIONARY_COMPATIBILITY_FOR_IMPORT` : Si `true`, une vérification de compatibilité de dictionnaire est réalisée à l'import d'un lot, indépendemment des vérifications de version. Ainsi, même pour une version différente présente dans la destination, si le dictionnaire est *compatible*  avec celui utilisé sur la source lors de l'export, un import peut être possible. Si le paramètre est `false` alors aucune vérification n'est réalisée (ce qui ne bloquera pas l'import si le dictionnaire est non compatible)
* `CHECK_MISSING_IDS_AT_MANAGED_DELETE` et `CHECK_MISSING_IDS_AT_MANAGED_UPDATE` : si `true`, pour le type de modification indiqué, une vérification sur la présence de références dans les données managées sera réalisée. Si une contrainte "fonctionnelle" liée à la spécification du dictionnaire (par exemple via la présence de lien entre 2 tables) alors la suppression ou la modification sera rejetée 
* `RECORD_IMPORT_WARNINGS` : Si `true` les détections d'anomalies possible à l'import d'un lot (suivant les cas de `warning` spécifiés dans le fichier `efluid-datagate-app/src/main/resources/merge-resolution-rules.json`) seront enregistrés dans la table `anomalies`. Rien n'est enregistré autrement
* `SELECT_PK_AS_DEFAULT_DICT_ENTRY_KEY` : Si `true` les clés naturelles des tables sont proposées comme KEY par défaut à la création d'une table de paramètrage. Sinon, aucune clé n'est proposée par défaut
* `VALIDATE_MISSING_REF_COMMITS_FOR_IMPORT` : Si `true` les commits "précédents" un commit importé doivent être présents dans la destination lors d'un import de lot. Autrement aucune vérification n'est réalisée
* `VALIDATE_VERSION_FOR_IMPORT` : Si `true` la version associée à un commit importé **doit** être présente dans la destination pour que l'import soit possible. Sinon aucune vérification n'est réalisée

> Toutes ces features ont en comment de "contraindre" le comportement de l'application, et de permettre une meilleure validation des données pour éviter la perte de cohérence. Néanmoins elles sont rendues modifiable à chaud pour permettre de désactiver certaines vérifications lors d'un import ou d'une manipulation. Il est **recommandé** de les réactivé une fois le "cas particulier" traité

Pour activer / désactiver ces features, utiliser l'API via l'ui swagger, sur le endpoint des **Feature flipping** : Voir [rest-api](rest-api.md) 
