# Transformation des données

Lorsque des données doivent être adaptées à partir de la source pour une destination, il est possible d'utiliser les Transformers. Ils "transforment" les données depuis les lots importés vers un format spécifique à la cible. La cible considère le lot appliqué, même si les données sont différentes par rapport à la source (ce traitement est donc non conforme aux paradigmes GIT). 

Ces transformers peuvent utiliser des données d'une table source (ce sont alors des *Sourced Transformers*) ou être autonome (*Direct Transformers*)

Quand il y a des données sources à prendre en compte, efluidDataGate s'appuie sur les informations des [Sources de transformation](transformer-source.md) du dictionnaire. C'est le type de transformation mise en oeuvre pour la régionalisation des données dans Efluid.

## Paramètrage des transformations

**Le paramètrage des transformations est donné ici** : 

```
datagate-efluid:

    # ...

     transformers:
        sourced-transformers:
           - com.efluid.TestTransformer
           - com.efluid.AutreTransformer
        direct-transformers:
           - com.efluid.UpperCaseValueApplyTranformer  
```

Les *Sourced Transformers* sont appliqués suivant leurs utilisations dans les sources de transformation. Les *Direct Transformers* sont tous appliqués dans l'ordre pour chaque lot.

Pour le détail du développement des transformers, voir [cet article](transformer-develop.md)

## Fonctionnement d'un transformer direct

Le transformer direct implémente 2 traitements :

* Vérification si la transformation peut être appliquée sur la table de paramètrage courante
* Modification du payload issu de l'index importé.

Le traitement est libre. Un exemple est le UpperCaseValueApplyTranformer qui passe des données de certaines colonnes en majuscule.

## Fonctionnement d'un transformer sourcé

La régionalisation Efluid nécessite d'appliquer les transformations à partir d'une table de données qui n'est présente que dans la BDD source. Les données sont historiquement préparées de manière spécifique pour un client ou un réseau à partir de la source. Avec efluidDataGate il n'y a pas de variété de format à partir de la source : l'instance cible doit être autonome pour transformer les données importées.

C'est pourquoi le dictionnaire permet de spécifier une source qui se retrouve partiellement exportée avec les lots pour pouvoir être prise en compte dans la destination.

En plus des traitements issues du transformer direct, le transformer sourcé en ajoute alors deux :

* Identification des données de la source qui seront nécessaire pour traiter un payload. Pris en compte lors de la création d'un lot pour préparer un package à exporter

Le traitement de transformation dispose par ailleurs de la configuration du transformer et des sources courantes.

## Solution technique

### Entités pour la gestion en BDD

Les sources sont spécifiées en tant que `fr.uem.efluid.model.entities.TransformerSource`

Les données packagées avec un lot pour une source sont gérées en tant que `fr.uem.efluid.model.entities.TransformerSet`. Il porte des entry, d'une manière similaire aux commits qui portent les index.

Le format "payload" des entries est par contre dépendant de la source, et donc du transformer. Cela peut être une partie seulement des données de la table source par exemple.