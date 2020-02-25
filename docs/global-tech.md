# Principes techniques généraux de Datagate

## Organisation technique de l'application

### Le modèle technique : une application web standard pour un besoin non standard

L'approche fonctionnel de Datagate est disruptive. Elle consiste à reproduire le fonctionnement de `git` pour la gestion de modifications dans une base de données.

Comme le modèle fonctionnel est en soit assez peu standard par rapports aux approches du marché, les choix techniques ont été établies en opposition, de manière à rester le plus standard possible dans les habitudes de développement actuelles.

### Stack technique
Spring-boot avec :
* **Spring MVC + Thymeleaf** : Contrôleurs WEB avec templates Thymeleaf + Contrôleurs Rest pour services d'intégration
* **Spring core** : IoC, core contexte
* **(Spring Security)** - accès sécurité très simplifié, à voir
* **Spring data JPA** : Manipulation BDD core, avec JPA, via répository spécifiés seulement sous la forme d'interfaces java
* **Spring JDBC avec Spring Transaction** : Pour les manipulations de la BDD managed, via JDBC directement

Utilise au maximum les conventions Spring-boot pour éliminer tout code technique. 

Pour la sécurité, chaque user a un "token" technique privé disponible : il suffit de le préciser dans l'url de l'appel REST.

L'UI est développée en simples templates html (Thymeleaf) avec jquery. 

### Organisation en couche simple. 

**Utilisation des terminologies standards suivantes** :
* xRepository => DAO. Archetype Spring
* xService => Service business. Archetype Spring
* xController => Contrôleur dans le cadre d'un traitement MVC (Spring-MVC est un framework de traitement par action). Archetype Spring
* contenus du package "modele" => Modèle de données business, associé à des entités gérés (en BDD ou en mémoire)
* contenus du package "services.types" => TO utilisés par les services business
* contenus du package "config" => Bean de configuration spring. Remplace les anciens fichiers "bean.xml" du temps de spring < v3. Archetype Spring

## Utilisation des bases de données
Pour le fonctionnement, il y a 2 DB de connectées :
* Celle propre au gestionnaire de paramètrage. Nommée **core** dans le code où elle est référencée. Utilisée avec Spring-data-jpa, avec config par défaut d'EntityManager
* Celle de l'application gérée. Nommée **Managed** dans le code. Utilisée avec JDBCTemplate, avec un transactionManager identifié configuré spécifiquement

## Configuration de l'application
Fichier de configuration *src/main/resources/application.yml*
Des options permises par Spring-boot donnent la possibilité de surcharger la conf depuis un fichier externe, ou des paramètres particuliers via arguments de ligne de commande.

Le fichier est au format YAML.

**Exemple de configuration standard de démonstration**

    ---
    ## PARAMETER MANAGER CONFIG
    datagate-efluid:
    
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


## Types de données gérés en interne

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
    