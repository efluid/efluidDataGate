# Transformateur Efluid pour les acteurs

Un transformateur dédié Efluid permet d'appliquer automatiquement les valeurs des "acteurs" sur les colonnes concernés, suivant des règles variés

> Pour rappel un transformateur joue une ou plusieurs modifications de données au moment de l'import d'un lot, à partir des règles qui lui ont été données. L'instance DATAGATE de 
> destination traite alors en merge les données après transformation.

## Contenu de la configuration

### Paramètres racines

* **tablePattern** : Obligatoire. regexp standard pour identifier une table de parametrage pour laquelle la transformation sera appliquée. Obligatoire
* **appliedKeyPatterns** : Obligatoire. liste de regexp standard précisant les valeurs de clés pour lesquelles appliquer la transformation. Par exemple "^\$.*OBJ" pour les lignes avec une clé de type "$1234OBJ"
* **appliedValueFilterPatterns** : Optionel. Liste de doublon "colonne / valeur" pour lesquelles la transformation s'applique
* **dateUpdates** : Génération de valeurs de types dates. Spécifiéées avec un élément de type "Spécification d'update" pour chaque colonne à "transformer". Les valeurs spécifiées peuvent être une date au format "AAAA-MM-JJ" (`"2020-05-11"`) ou la valeur `"current_date"` pour utiliser la date courante. Peut s'appliquer sur tous les temporals (date, datetime, timestamp ...)
* **actorUpdates** : Génération de valeurs de type chaine de caractère ou décimal. Spécifiéées avec un élément de type "Spécification d'update" pour chaque colonne à "transformer".

> Au moins un `dateUpdates` ou un `actorUpdates` doit être spécifié.
  
### Paramètres des "Spécification d'update"

* **value** : Obligatoire. La valeur à appliquer (date ou `current_date` pour les `dateUpdates`, une constante pour les `actorUpdates`)
* **onActions** : Liste des "actions" des lignes importées pour lesquelles appliquer la transformation. Jusqu'à 3 items parmis "ADD", "REMOVE", "UPDATE"
* **onValues** : Liste de filtres de recherche de valeur pour lesquels appliquer la transformation.
* **onValues.columnPattern** : Obligatoire sur le filtre. Regexp pour un nom de colonne sur laquelle le filtre sera testé
* **onValues.valuePattern** : Obligatoire sur le filtre. Regexp pour une valeur de colonne sur laquelle le filtre sera testé

> Au moins un `onActions` ou `onValues` doit être spécifié pour chaque "Spécification d'update". Si les deux sont spécifiés, une combinatoire "OU" est appliquée

> Les `onValues` permettent de préciser par exemple `{"columnPattern" : "ETATOBJET", "valuePattern" : "DELETED"}` pour n'appliquer la modification que pour les lignes qui 
> ont une colonne "ETATOBJET" avec la valeur "DELETED" - cas de la suppression logique 

## Exemple de configuration

```
{
  "tablePattern" : ".*",
  "appliedKeyPatterns" : [ "TRA$.*" ],
  "appliedValueFilterPatterns" : {
    "ETATOBJET" : "0"
  },
  "dateUpdates" : {
    "DATESUPPRESSION" : {
      "value" : "current_date",
      "onValues" : [ {
        "columnPattern" : "DELETED",
        "valuePattern" : "1"
      } ]
    },
    "DATEMODIFICATION" : {
      "value" : "current_date",
      "onActions" : [ "ADD", "REMOVE", "UPDATE" ]
    },
    "DATECREATION" : {
      "value" : "current_date",
      "onActions" : [ "ADD", "REMOVE", "UPDATE" ]
    }
  },
  "actorUpdates" : {
    "ACTEURMODIFICATION" : {
      "value" : "evt 154654",
      "onActions" : [ "ADD", "REMOVE", "UPDATE" ]
    },
    "ACTEURCREATION" : {
      "value" : "evt 156444",
      "onActions" : [ "ADD", "REMOVE", "UPDATE" ]
    },
    "ACTEURSUPPRESSION" : {
      "value" : "evt 189445",
      "onValues" : [ {
        "columnPattern" : "DELETED",
        "valuePattern" : "1"
      } ]
    }
  }
}
```