# Transformateurs

## Principes des transformateurs

Les transformateurs sont des composants permettant de modifier les données d'un lot au moment de leur prise en compte dans un merge.

Ils permettent d'adapter les données d'une instance de paramètrage source aux contraintes propres à une destination :
* A partir d'un paramètrage associé au transformateur, ils "traitent" les données du lot importé
* Les données sont alors modifiés suivant ce que peut traiter le transformer. 
* Le transformer peut être selectif : seules certaines colonnes / tables sont concernées par la transformation
* La configuration prise en compte peut également indiquer quelles tables / colonnes sont modifiées
* La transformation se fait par "payload de diff" : un payload en entré est transformé dans un autre payload en sortie
* Il peut être selectif dans les données d'une colonne et ne traiter que certaines lignes de diff
* Le merge est réalisé sur la base des données obtenues en "sortie" 

Principe de la transformation :
![transformer spec](pictures/transformers.png?raw=true "model")

> Le principe de la transformation de données "casse" le modèle calqué sur GIT pour Datagate. En effet, une fois les données transformées, elles ne correspondent plus au contenu initial du commit. Ce dernier n'est donc plus immutable.

Plusieurs transformer peuvent intervenir sur le même lot - ils sont alors traités suivant une priorité donnée, l'un après l'autre :
![transformer chain](pictures/transformers-chain.png?raw=true "model")

> Les données peuvent donc être transformées plusieurs fois. Certains transformateur supportent ce comportement comme par exemple les types "MATH" et "CASE_CHANGE"

## Description d'un transformateur

Les données nécessaires à un transformateur constituent donc en :
* Le transformateur lui même : c'est un composant java, identifié par sa classe. Différentes classes de transformateurs sont fournies dans Datagate
* La configuration du transformateur : tout ce qui peut être nécessaire à son fonctionnement. Cela peut être des paramètres fixés, ou des règles d'informations permettant de selectionner des données "sources" pour certains transformer complexes
* La priorité d'application du transformateur
* Le projet associé. Les transformateurs sont fixés au niveau du projet, mais ne font pas partie du dictionnaire en tant que tel : une configuration de transformateur peut être modifiée sans impacter le versionning du dictionnaire

Toutes ces informations sont regroupées sous le terme : Description de transformateur / `TransformerDef` :
![transformer def](pictures/transformers-entity.png?raw=true "model")
 
L'application Datagate permet de créer et de configurer ces TransformerDef

## Créer / configurer des transformateurs pour le projet

Dans le menu, accéder à "Projet > Transformations de données"

Depuis cet écran il est possible de spécifier une liste de transformateur, avec pour chaque un nom, un type, la priorité, et la configuration spécifique (dépendante du type)

Pour le détail des types de transformateurs disponibles et leurs configurations spécifiques, voir l'article dédié [Les transformers disponibles et leur configuration](transformer-config.md)

La configuration est un json. Par défaut un *exemple* de configuration valide est automatiquement présenté. 

>Le json est validé à l'enregistrement, ce qui permet d'identifier la majorité des problémes de configuration. Néanmoins il est important de bien consulter la documentation du transformateur pour éviter les erreurs ...

## Export / import des transformateurs avec les lots

Les configurations des transformateurs du projet sont **systématiquement** exportés avec les lots. Ainsi lors de l'import la configuration nécessaire est chargée et appliquée automatiquement.

> L'export de lot n'embarque que la configuration, et non pas le transformateur lui même, qui est un composant java. Si la version Datagate cible ne supporte pas le transformateur, alors l'import est rejeté : Datagate doit être mis à jour au préalable avec une version compatible. 