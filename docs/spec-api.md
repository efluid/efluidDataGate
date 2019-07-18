# Utilisation de l’API de spécification du paramètrage

## Introduction

### Quelques principes sur la gestion du paramètrage

Une nouvelle application de gestion de paramètrage en base de données est en cours de mise en place. Cet outil permet d’identifier toutes les modifications de paramètrage et de les livrer sur des environnements distants, en gérant les conflits, 

les différences de versions et les dépendances entre données. Il permet également d’annuler des modifications de paramètrage dans une base de données Efluid.

Pour son fonctionnement il s’appuie sur un dictionnaire où sont identifiées toutes les tables d’une base de données qui doivent être considérées comme du paramètrage.

**Ce dictionnaire exploite l’organisation suivante :**

![model](docs/model.png?raw=true "model")

La définition d’un dictionnaire valide est essentiel au bon fonctionnement de l’application. Le système de versions permet de le faire évoluer au cours du temps.

### Rôle de l’API de spécification du dictionnaire

Bien que l’application propose une interface de paramètrage complète pour spécifier un dictionnaire, une API est également proposée pour le définir au niveau du code java.

En effet la base de données Efluid est associée à un modèle de données spécifié dans le code Java, et profiter de ce code pour définir le dictionnaire de paramètrage apporte un gain de temps conséquent.

L’API permet donc au niveau des DAO / POJO d’entités de définir les informations techniques de ce qui correspond à du paramètrage dans la base de données. Elle est composée d’annotation uniquement, et est interprété par un générateur (existant sous forme de plugin maven) pour générer le dictionnaire.

Elle est assez souple et propose souvent plusieurs approches de configurations suivant les besoins et l’organisation recherchée
Utilisation de l’API

## Les éléments de haut niveau : domaine et projets

### Spécifications des domaines fonctionnelles

La première information importante est la définition des domaines fonctionnelles. Chaque table de paramètrage peut être associée à un domaine (et un seul) qui représente le métier ou le concept correspondant. 
Les domaines fonctionnels ne sont représentés que par un nom.

**Quelques exemples de domaines fonctionnels :**

* Gestion du matériel
* Profils utilisateurs
* Modèles éditiques

Un domaine fonctionnel est spécifié avec l’annotation **fr.uem.efluid.ParameterDomain**

*Paramètres de l’annotiation @ ParameterDomain:*

* **Name** : le nom du projet
* **Project** : Le projet correspondant, voir le point suivant

*L’annotation ___ParameterDomain__ peut être exploitée de deux manières :*

* Elle peut être appliquée au niveau d’un package : tous les composants dans le package sont considérés comme appartenant à ce domaine. Pour cela créer un fichier package-info.java à la racine du projet et l’annoter avec **@ParameterDomain**
* Elle peut être utilisée pour créer une méta-annotation : créer une nouvelle annotation et l’annoter elle-même avec **@ParameterDomain**. Pour indiquer qu’un composant appartient au domaine, il suffit alors de l’annoter avec l’annotation ainsi créée

**A noter** : les spécifications de tables qui seront vues plus loin permettent de spécifier également « à la volée » des domaines via un attribut dédié. Il est cependant recommandé d’utiliser l’annotation dédiée pour plus de clarté.

### Spécification du ou des projets

Toutes les informations gérées, dont les domaines fonctionnels, sont regroupés par projets. Les projets sont indépendants, et peuvent utiliser des données identiques d’un projet à un autre. Les domaines et les projets sont intrinsèquement liées dans l’API.

Un projet est représenté par un nom (exemple : « Base Efluid Production ») et une couleur. La couleur permet d’identifier directement le projet en cours d’édition dans l’outil de gestion du paramètrage.

L’API permet de spécifier un ou plusieurs projets via l’annotation **fr.uem.efluid.ParameterProject** qui ne peut être spécifiée qu’au sein d’un domaine fonctionnel, pour l’attribut **project()**

*Paramètres de l’annotation __@ParameterProject__ :*

* **Name** : le nom du projet
* **Color** : la couleur, choisie depuis un enum **fr.uem.efluid.ProjectColor**

*Le projet est donc défini avec le domaine, avec quelques règles d’utilisation :*

* Si 2 projets sont identifiés avec le même nom, alors le générateur va considérer que c’est le même projet.
* Si aucun projet n’est spécifié dans un domaine (l’attribut « project » de @ParameterDomain n’est pas obligatoire) alors un projet par défaut est utilisé : « Default », couleur « Grise ». Ce projet « Default » est systématiquement présent dans l’application de gestion du paramètrage 

### Exemples de spécifications de domaines et de projets

Les deux étant liées, ils sont généralement spécifiés en même temps.

*Exemple de spécification par package :* 

Dans le package **fr.uem.efluid.sample.other**, création du fichier **package-info.java** :

    
    @ParameterDomain(name = "Autre domaine", project = @ParameterProject(name = "My project", color = ProjectColor.BLUE))
    package fr.uem.efluid.sample.other;

Tous les composants dans le package **fr.uem.efluid.sample.other** sont alors associés à ce domaine / ce projet

*Exemple de spécification par méta-annotation :*

Création de l’annotation **GestionDuMateriel** :

    @Documented
    @Retention(CLASS)
    @Target(TYPE)
    @ParameterDomain(name = "Gestion du materiel", project = @ParameterProject(name = "My project", color = ProjectColor.BLUE))
    public @interface GestionDuMateriel {
        // Meta-annotation, no content
    }

Tous les composants annotés avec **@GestionDuMateriel** sont alors associés à ce domaine / projet

## Spécification de table de paramètrage

### Sur la définition des clés

Les **tables de paramètrage** sont les tables en BDD qui seront inspectées et suivies par l’application de gestion pour identifier les données de paramètrage. Ces données peuvent ensuite être exportées / mergées / rollbackées suivant les besoins.

Pour l’application de gestion il est important de connaitre les colonnes importantes dans ces tables, et avant tout d’identifier la clé représentant unitairement une donnée de paramètrage.

Cette clé n’est pas nécessairement l’id technique : c’est un identifiant fonctionnel unique qui doit impérativement être immutable et représenter la donnée sur tous les environnements où elle est définie.

Par exemple sur une table de ce type : 

**MA_TABLE**
* ID long
* CODE varchar
* VALUE varchar

Si cette table est présente sur 2 environnements différents, il n’est pas forcément possible d’utiliser l’ID comme clé : en effet, si je créée une entrée *« TESTAAA / VALEURXXXY »* sur les 2 environnements, l’ID peut être généré automatiquement avec une valeur différente alors que c’est la même donnée qui est représentée. Dans ce cas la clé « fonctionnelle » est *CODE*, et non pas *ID*.

L’application de gestion doit impérativement connaitre les clés fonctionnelles à utiliser, ainsi que leur type pour les gérer correctement. Les clés composites sont supportées (jusqu’à 5 colonnes de clé par table)

**A noter** : dans Efluid l’ID technique est généralement l’ID fonctionnel. Mais la question « quelle propriété utiliser pour représenter la clé » reste tout de même essentielle pour chaque table.

Des annotations spécifiques permettent de préciser les clés : elles seront abordées plus loin

### Données nécessaires pour la spécification des colonnes à utiliser

L’application de gestion de paramètrage utilise un format interne « à plat » : les données extraites sont représentées sous forme de ligne de données compressées et hashées. Pour produire ces données il est nécessaire d’indiquer quelles **colonnes** utiliser, mais sans plus de détails. En effet le typage est par exemple inutile pour le fonctionnement de l’application.

Les colonnes à utiliser peuvent être spécifiées avec différentes solution dans l’API fournie

### Représentation d’une table de paramètrage

La source de données de paramètrage gérée est la « **table de paramètrage** ». Elle correspond à une (et une seule) table en base de données, associée à une clé (qui peut être composite) et des valeurs.

*D’autres informations peuvent aussi être indiquées :*

* Un nom fonctionnel (qui peut donc être différent du nom de la table, et qui permet de représenter les informations de manière plus adaptée)
* Un critère de sélection : C’est un filtre de selection SQL pour filtrer les données à prendre en compte. Le filtre par défaut est « 1 = 1 » c’est-à-dire que toutes les données d’une table sont prises en compte. La table étant considérée comme systématiquement aliassée avec le nom « cur », il est possible par exemple de spécifier un critère « cur.ENABLE = true »
* Des liens / mappings (voir plus loins)
* Un domaine fonctionnel
* Et donc une clé et des valeurs

L’annotation de spécification d’une table est **fr.uem.efluid.ParameterTable**

_Cette annotation **@ParameterTable** intègre les propriétés suivantes :_

* **name** : le nom fonctionnel de la table de paramètrage
* **tableName** : le nom de la table SQL 
* **filterClause** : un critère de sélection optionnel
* **domainName** : le domaine, si celui-ci n’est pas spécifié par package ou par méta-annotation
* **keyField** : la propriété (java) à utiliser comme clé, si les annotations spécifiques pour cela ne sont pas utilisées
* **keyType** : le type de valeur pour la clé si **keyField** est spécifié
* **useAllFields** : par défaut true. Indique d’utiliser tous les fields de l’objet courant comme colonne de valeur a moins qu’ils soient explicitement exclus
* **values** : La spécification explicite des colonnes de valeur si les annotations spécifiques ne sont pas utilisées et si **useAllFields** est false

Cette annotation est portée par une classe java : on indique donc une table à partir d’un objet java la représentant (type entité ou DAO).

**A noter** : le nom de table doit être unique pour tout le projet. Le générateur contrôle les duplications

### Spécification des clés et valeurs

Les valeurs et clés peuvent être spécifiés via les annotations dédiées **fr.uem.efluid.ParameterKey**, **fr.uem.efluid.ParameterValue** et **fr.uem.efluid.ParameterValeurComposite**, qui peuvent tous les 3 êtres portées soit par les fields soit par les méthodes de la classe.

*Les propriétés de __@ParameterKey__ :*

* **value** : le nom de la colonne correspondant au field / à la méthode annotée à utiliser comme clé
* **type** : le type de valeur pour la clé. Utilise l'enum **fr.uem.efluid.ColumnType** avec : BINARY = les blobs / clobs, ATOMIC = les numériques (int, float ...), STRING = les litérales (varchar...), BOOLEAN = les booléens, TEMPORAL = les dates, times, timestamps ...
* **forTable** : Dans le cas d'une utilisation d'un "set de table de paramètrage" (voir plus loin), permet d'indiquer pour quel table la clé est mappée

N'importe quel field ou méthode peut être utilisé comme clé. Si **value** n'est pas précisé, alors ces le nom du field ou de la méthode en majuscule qui est utilisé. Le **type** est également déterminé automatiquement à partir du type java. Tous les types java standards de définition de clées fonctionnelles sont supportés : String, Long ... Le type doit être précisé quand il n'est pas explicite (cas d'un type interne par exemple)

Pour préciser une clé composite, il suffit d'indiquer **@ParameterKey** pour chacune des propriétés utilisées dans la clé. 5 maximum sont possibles par table.

*Les propriétés de __@ParameterValue__ :*
* **value** = **name** : Utiliser l'un ou l'autre pour définir le nom de la colonne correspondante au field / méthode
* **forTable** : Idem **@ParameterKey**

La même règle de génération du nom de colonne que pour les clé est utilisée si value ou name ne sont pas précisé. Par ailleurs une valeur peut être implicite : Si dans **@ParameterTable** **useAllFields** est true (ce qui est le cas par défaut) alors tous les fields de l'objet java sont considérés comme des Values, à moins qu'ils soient annoté comme des clés ou avec l'annotation d'exclusion **@ParameterIgnored**.

Par ailleurs, les values peuvent être explicitement spécifiées dans **@ParameterTable**, dans l'attribut **values**. Chaque **@ParameterValue** indiqué ainsi est alors pris en compte dans la spécification de la table de paramètrage.

N'importe quel type de champs scalaire peut être employé comme value


L'utilisation de l'annotation **@ParameterValueComposite** est réservée aux définitions de links / mappings, et sera abordée plus loin.

### Sur l'héritage des annotations

L'annotation ParameterTable est héritée. Donc une classe héritant d'une classe elle même ParameterTable est prise en compte automatiquement comme une table de paramètrage. Des propriétés peuvent alors devoir être surchargées.

Pour ignorer une classe héritant d'une classe **@ParameterTable**, il est possible de l'annoter **@ParameterIgnored**.

Des règles de précéhenses existent entre les définitions héritées et celles explicitement données : les informations les plus "basses" dans la chaine d'héritage sont toujours les prioritaires.

### Exemples de spécification de table simple

A partir des définitions par **@ParameterTable**, **@ParameterKey** et **@ParameterValue**, voici des exemples de mise en oeuvre

*Exemple où tous les éléments sont spécifiés explicitement sur les fields  :*

    @GestionDuMateriel
    @ParameterTable(name = "Categorie", tableName = "TCATEGORY", useAllFields = false)
    public class CategorieDeMateriel {
    
        // Ignoré implicitement
        private Long id;
    
        @ParameterValue("NAME")
        private String name;
    
        @ParameterKey("CODE")
        private String code;

        // ...
    }

*Exemple où tous les éléments sont spécifiés implicitements :*

    @ParameterTable(keyField = "key")
    public class MyType {

        // Ignoré explicitement
        @ParameterIgnored
        private Long id;
        
        private String key;
        
        private String value;
        
        private String otherValue;

        // ...
    }

*Exemple d'utilisation sur les méthodes :*

    @ParameterTable(name = "Autre table", tableName = "TOTHER", filterClause = "1=1", useAllFields = false)
    public class AutreItem {
        
        private Long id;
        private Long code;
        private LocalDateTime when;
        private Float value;
        private byte[] file;
        
        public Long getId() {
            return this.id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        @ParameterKey("CODE")
        public Long getCode() {
            return this.code;
        }
        
        public void setCode(Long code) {
            this.code = code;
        }
        
        @ParameterValue("WHEN")
        public LocalDateTime getWhen() {
            return this.when;
        }
        
        public void setWhen(LocalDateTime when) {
            this.when = when;
        }
        
        @ParameterValue("VALUE")
        public Float getValue() {
            return this.value;
        }
        
        public void setValue(Float value) {
            this.value = value;
        }
        
        @ParameterValue("BFILE")
        public byte[] getFile() {
            return this.file;
        }
        
        public void setFile(byte[] file) {
            this.file = file;
        }
    }

*Exemple de spécification uniquement via l'annotation __@ParameterTable__ :*

    @ParameterTable(tableName = "T_TABLE_MUTSECOND_ONE", keyField = "keyOne", useAllFields = false, values = {
        @ParameterValue("valueOnAll"),
        @ParameterValue("valueA"),
        @ParameterValue("valueB")
    })
    public class TypeOnMultipleTablesSecond {

        private Long keyOne;

        private String valueOnAll;

        private String valueA;

        private String valueB;

        // ...
    }

### Set de table de paramètrage 

Dans certains cas une classe java peut correspondre à plusieurs tables en base de données : répartition, inner components ...

Pour cela il est possible d'utiliser l'annotation fr.uem.efluid.ParameterTableSet pour indiquer plusieurs @ParameterTable sur un seul composant java

*Les propriétés de __@ParameterTableSet__ sont :*

* **value** = **tables** : La définition des **@ParameterTable**
* **useAllFields** : Même usage que dans **@ParameterTable** mais permet de définir sa valeur pour tout le set
* **domainName** : Même usage que dans **@ParameterTable** mais permet de définir sa valeur pour tout le set

Les sets peuvent contenir autant de table que nécessaire. Attention, ils sont également hérités.

Les values et keys peuvent être spécifiées directement dans les **@ParameterTable** ou sur les fields et methodes de la classe. Dans ce cas, par défaut il sont utilisées dans **toutes** les tables du set, à moins d'utiliser l'attribut **forTable** où sont précisées la ou les noms de table associées. Donc par défaut si **useAllFields** est true, et si aucune Value n'est explicitement indiquée, tous les fields seront mappées comme des values pour toutes les tables du set

*Exemple de spécification de __@ParameterTableSet__ :*

    @ParameterTableSet({
        @ParameterTable(tableName = "T_TABLE_MUTONE_ONE"),
        @ParameterTable(tableName = "T_TABLE_MUTONE_TWO"),
        @ParameterTable(tableName = "T_TABLE_MUTONE_THREE")
    })
    public class TypeOnMultipleTablesFirst {
        @ParameterKey // Si pas indiqué => Commun à toutes les tables du set
        private Long key;

        @ParameterValue // Si pas indiqué => Commun à toutes les tables du set
        private String valueOnAll;

        @ParameterValue(forTable = "T_TABLE_MUTONE_ONE")
        private String valueA;

        @ParameterValue(forTable = "T_TABLE_MUTONE_ONE")
        private String valueB;

        @ParameterValue(forTable = "T_TABLE_MUTONE_TWO")
        private String valueC;

        @ParameterValue(forTable = "T_TABLE_MUTONE_TWO")
        private String valueD;

        @ParameterValue(forTable = "T_TABLE_MUTONE_THREE")
        private String valueE;

        @ParameterValue(forTable = "T_TABLE_MUTONE_THREE")
        private String valueF;
        // ...
    }

## Spécification de liens et mappings

### Principes du lien et du mapping

Le **lien** est un type de valeur. Il est définie comme une association 1-N pour une colonne de la table de paramètrage, renvoyant vers la clé d'une autre table de paramètrage (une Foreign Key donc). Il ne peut pas être bidirectionnel, l'application ne gère que la liaison dans le sens "ManyToOne", mais pas "OneToMany".

Il permet à l'application de gestion du paramètrage de garantir la cohérence des données en vérifiant que les liaisons entre table sont respectées et valides. 

Il ne peut être porté que sur une valeur scalaire, représenté généralement en java par une autre classe d'entité mappée, comme dans cet exemple :

    public class MonType {
    
        private Long key;
    
        // Correspond à un lien 1-N
        private MonAutreType autreType;
    
        // ...
    }

Le **mapping** est la représentation d'une association N-N (avec une table de mapping N/N intermédiaire). Il est représenté par une propriété de type collection au niveau java, comme dans cet exemple : 

    public class MonType {
    
        private Long key;
    
        // Correspond à un lien N-N
        private Collection<MonAutreType> autreType;
    
        // ...
    }

Liens et mappings sont spécifiés via des annotations spécifiques, qui complètent l'annotation **@ParameterValue**

### Spécifier un lien

Le lien est identifié avec **fr.uem.efluid.ParameterLink**. Il vient **obligatoirement** en complément de l'annotation **ParameterValue** (ou du fait que la propriété mappée est implicitement gérée comme une value)

*Les propriétés de __@ParameterLink__ :*
* **toParameter** : La classe du type cible si elle ne peut pas être identifiée directement depuis la définition du field / de la méthode annotée
* **toTableName** : Le nom de la table cible. Si il est vide, le générateur cherchera à identifier le type d'entité associé au field (par exemple dans Collection<MonAutreType> il analysera MonAutreType) ou au **toParameter** et prendra en compte les définitions de table du type lié
* **toColumn** : la colonne ou les colonnes mappée(s) dans la cible. Si il est vide, la clé ou les clés fonctionnelle(s) sera/seront utilisée(s)
* **name** : un nom fonctionnel optionnel pour le lien

Cette annotation est héritable.

**A noter** : Même si la colonne de liaison (valeur de **toParameter**) est un identifiant technique non mappé comme clé, ce n'est pas un soucis pour l'application de gestion du paramètrage : c'est toujours la clé fonctionnelle qui sera gérée au final (les requêtes se feront en utilisant une jointure sur l'identifiant technique, mais une selection par l'identifiant fonctionnel)

### Spécifier un mapping

Le mapping est globalement basé sur les principes du lien, mais avec plus d'informations pour gérer la définition de la table de mapping intermédiaire. L'annotation spécifique pour cela est **fr.uem.efluid.ParameterMapping**

*Les propriétés de __@ParameterMapping__ :*
* **toParameter** : La classe du type cible si elle ne peut pas être identifiée directement depuis la définition du field / de la méthode annotée
* **mapTableName** : Le nom de la table de mapping
* **mapColumnFrom** : Le nom de la colonne référençant la colonne de lien locale dans la table de mapping. Si il n'est pas précisé c'est le même nom de colonne que **fromColumn** qui est utilisé (et donc le cas échéant, le nom de colonne de la clé)
* **mapColumnTo** : Le nom de la colonne référençant la colonne de lien de la cible dans la table de mapping. Si il n'est pas précisé c'est le même nom de colonne que **toColumn** qui est utilisé (et donc le cas échéant, le nom de colonne de la clé du type référencé)
* **toTableName** : Le nom de la table cible. Si il est vide, le générateur cherchera à identifier le type d'entité associé au field (par exemple dans Collection<MonAutreType> il analysera MonAutreType) ou au **toParameter** et prendra en compte les définitions de table du type lié
* **toColumn** : la colonne ou les colonnes mappée(s) dans la cible. Si il est vide, la clé fonctionnelle sera utilisée
* **fromColumn** : la colonne mappée dans l'entité locale si elle ne peut pas être déterminée (si ce n'est pas la clé par exemple)
* **name** : un nom fonctionnel optionnel pour le mapping

**A noter** : Bien que géré dans l'API et dans le dictionnaire, la version actuelle de l'application de gestion du paramètrage ne traite pas encore les données dans les mappings.

### Cas des clés composites

Efluid utilise des clés composites sur certaines données, y compris pour la spécification des liens et des mappings. Il est possible de le gérer avec l'API. Dans ce cas au lieu d'être défini sur un élément annoté avec **@ParameterValue**, c'est l'annotation **fr.uem.efluid.ParameterCompositeValue** qui est utilisée

*Les propriétés de __@ParameterCompositeValue__ :*
* **value** = **names** : Utiliser l'un ou l'autre pour définir les noms des colonnes utilisées pour le lien / le mapping
* **forTable** : Idem **@ParameterKey**

L'ordre donné dans l'attribut **names** est important : il doit être identique dans la spécification des colonnes dans le lien ou le mapping associé

### Exemples d'utilisation de liens et de mappings

Les liens et mappings sont obligatoirement définies sur des propriétés mappées soit comme __@ParameterValue__  soit comme __@ParameterCompositeValue__, ou alors implicitement mappées comme value si la table a l'attribut **useAllFields** actif. Il y a donc plusieurs formes de spécification des liens suivant les caractères implicites / explicites ou encore hérités des propriétés.

*Exemple de lien pour une spécification totalement explicite :*

    @GestionDuMateriel
    @ParameterTable(name = "Compteur", tableName = "TCOMPTEUR", useAllFields=false, filterClause = "cur.\"ACTIF\"=1")
    public class Compteur extends Materiel {
        
        @ParameterValue("NOM")
        private String nom;
        
        @ParameterValue("FABRIQUANT")
        private String fabriquant;
        
        private boolean actif;

        @ParameterValue("TYPE_ID")
        @ParameterLink(toColumn = "ID")
        private TypeDeCompteur type;
        // ...
    }

La référence ici entre TYPE_ID et ID sur la table utilisée pour le type **TypeDeCompteur** est claire

*Exemple de lien pour une spécification avec des éléments hérités :*

    public class MySubType extends MyType {
        
        @ParameterLink
        private SubElement subElement;

        // ...
    }

Ici la classe héritée **MyType** est mappée comme **@ParameterTable** avec **useAllFields**=true. La clé est implicite également et utilise une colonne "KEY". Le lien étant défini sans attribut, il est spécifié par rapport aux clés uniquement : une colonne SUBELEMENT dans la table MYSUBTYPE référence la clé de la table du type **SubElement**

*Exemple de mapping pour une spécification avec des éléments hérités :*

    public class MyOtherSubType extends MyType {
        
        private String customKey;
        
        private Long otherAttribute;
        
        @ParameterMapping(mapTableName = "T_OTH_LINKS")
        private Collection<AnotherLinkedType> linkedTypes;

        @ParameterMapping(mapTableName = "T_OTH_LAST_LINKS", fromColumn = "ID", toColumn = "ID", mapColumnFrom = "SUB_ID", mapColumnTo = "LIN_ID")
        private Collection<LastLinkedType> lastLinkedTypes;

        // ...
    }

Ici la table est mappée car **MyOtherSubType** hérite de **MyType** qui est annoté **@ParameterTable** avec **useAllFields**=true. La clé est implicite également et utilise une colonne "KEY".

*Les deux mappings sont interprétés comme tel ici :*

* **linkedTypes** => Lien N-N entre la table MYOTHERSUBTYPE et la table de AnotherLinkedType via la table de mapping T_OTH_LINKS, sur les colonnes MYOTHERSUBTYPE.KEY => T_OTH_LINKS.KEY et T_OTH_LINKS.AnotherLinkedTypeKey => AnotherLinkedType.AnotherLinkedTypeKey
* **lastLinkedTypes** => Lien N-N entre la table MYOTHERSUBTYPE et la table de LastLinkedType via la table de mapping T_OTH_LAST_LINKS, sur les colonnes MYOTHERSUBTYPE.ID => T_OTH_LAST_LINKS.SUB_ID et T_OTH_LAST_LINKS.LIN_ID => AnotherLinkedType.ID

*Exemple de lien avec une clé composite :*

    @ParameterTable
    public class TabWithRefOnCompositeKey {

        @ParameterKey
        private String localKey;
        
        @ParameterLink(toColumn = { "BIZ_KEY_ONE", "BIZ_KEY_TWO", "BIZ_KEY_THREE" })
        @ParameterCompositeValue({ "REF_BIZ_KEY_ONE", "REF_BIZ_KEY_TWO", "REF_BIZ_KEY_THREE" })
        private TabWithCompositeKey referenced;
        
        // Automatically mapped as value
        private String value;
        
        // Automatically mapped as value
        private String other;
        
        @ParameterIgnored
        private String something;
        
        // ...
    }

Ici c'est le type lié **TabWithCompositeKey** qui utilise une clé composite (3 colonnes : "BIZ_KEY_ONE", "BIZ_KEY_TWO" et "BIZ_KEY_THREE") et donc la table de **TabWithRefOnCompositeKey** a 3 colonnes avec FK pour gérer le lien. L'ordre utilisé entre les toColumn et les composites values est obligatoirement identique.

## Utilisation du générateur

L'API à base d'annotation permet de spécifier le dictionnaire, mais c'est un générateur, sous la forme d'un plugin maven, qui prend en compte concrètement ces données lors du build du projet pour générer les informations attendues.

L'application de gestion de paramètrage attend un fichier spécifique au format ".par" pour préciser le dictionnaire. Le générateur produit ce fichier à partir du code, pour qu'il soit importé manuellement, ou bien peut directement uploader ce fichier dans une instance d'application spécifiée.

### Mise en place du plugin

Ajouter au build du pom de votre projet : 

    <plugin>
        <groupId>${project.groupId}</groupId>
        <artifactId>efluid-datagate-generator</artifactId>
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
                <!-- voir les configurations plus loin -->
            </configuration>
        </plugin>

*Les options de configuration supportées par le générateur sont :*

* **destinationFileDesignation** : Le nom pour le fichier généré (utilisera l'extension ".par")
* **destinationFolder** : dossier où est produit le .par
* **protectColumn** : Indique si dans la base de données utilisée les noms de colonnes dans le code SQL doivent être encadrées de double côte. Recommandé sur toutes les BDD le supportant (dont oracle)
* **sourcePackage** : Le package racine où rechercher tous les éléments annotés pour le dictionnaire
* **uploadToServer** : true pour uploader automatiquement le fichier .par vers une instance d'application cible
* **uploadEntryPointUri** : URL de l'instance d'application cible (URI précise du point d'entrée pour la gestion du dictionnaire)
* **uploadSecurityToken** : Token d'identification dans l'application cible. Chaque utilisateur dispose d'un token unique, à renseigner donc ici pour autoriser l'upload
* **projectVersion** : Version du dictionnaire. Il est recommandé d'utiliser la version du projet. L'application supporte les SNAPSHOT.

*Exemple de configuration :*

    <configuration>
        <destinationFileDesignation>generated-dictionary</destinationFileDesignation>
        <destinationFolder>${project.basedir}/target</destinationFolder>
        <protectColumn>true</protectColumn>
        <sourcePackage>fr.uem.efluid.sample</sourcePackage>
        <uploadToServer>false</uploadToServer>
        <uploadEntryPointUri>http://127.0.0.1:8080/rest/v1/dictionary</uploadEntryPointUri>
        <uploadSecurityToken>afc9921811684c7f88062cd47ddf0ff5</uploadSecurityToken>
        <projectVersion>${project.version}</projectVersion>
    </configuration>

