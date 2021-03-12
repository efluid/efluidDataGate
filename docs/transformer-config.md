# Les transformers disponibles et leur configuration

## Principes communs sur la configuration des transformers

La configuration est spécifiée en format json. Le format du json utilisé dépend du type de transformateur : ils sont tous différents.

L'éditeur de json donné dans l'écran de spécification des transformateurs est simpliste et ne vérifie pas le contenu à la volée. 
Néanmoins les transformateurs embarquent un dispositif de vérification de configuration, pour s'assurer qu'elle est valide. L'enregistrement est donc bloqué tant que la configuration est incorrecte.

## Transformateurs disponibles

### Transformateurs standards UPPERCASE_TRANSFORMER & LOWERCASE_TRANSFORMER 

> Avant tout destiné à valider le concept de transformation, ces transformateurs sont plutôt réservés à des tests

Ils transforment les valeurs matchés en majuscule ou en minuscule

Paramètres : 
* **tablePattern** : regexp standard pour identifier une table de parametrage pour laquelle la transformation sera appliquée. Obligatoire
* **columnNames** : liste de regexp standard précisant les colonnes pour lesquelles le contenu sera transformé

Example de configuration : 
```
{
  "tablePattern" : ".*",
  "columnNames" : [ ".*", "COL_.*", "COL_C" ]
}
```

### Transformateur Efluid des acteurs

Voir [l'article dédié](efluid-transformer.md)

