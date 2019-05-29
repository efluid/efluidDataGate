# Ecrire des scénarios de recette Efluid sous la forme de gherkins

## Possibilités

Les tests existants permettent d'écrire des scénarios allant jusqu'à la gestion d'un merge. Il est possible de reproduire ainsi les principaux cas de recette préparés par Efluid.

Les données utilisées sont gérées avec des **entités jpa** représentant les "tables" sources (de type `testa`, `testb`...). Des fixtures prééxistantes permettent de spécifier le contenu de ces entités simplement. Pour ajouter de nouvelle table source, il est nécessaire d'écrire un peu de code java

## Pour ajouter un scénario

### Projet utilisé

Toutes les opérations sont réalisées dans le [projet param-app-cucumber](../param-app-cucumber/README.md)

### Ajouter une table - code java

Par exemple pour ajouter une table de donnée `testc`, les étapes à suivre sont préciséments :

**1/ Créer un entité JPA correspondante**

L'entité est ajoutée dans le package `fr.uem.efluid.system.stubs.entities`, et nommée `EfluidTesta` (voir les autres entités en exemple)
Tous les attributs doivent être précisés, avec leurs getters / setters associés.

> Il est également **obligatoire** de spécifier hashcode et equals avec tous les attributs utilisés (equals sera utilisé dans les tests)

**2/ Spécifier l'entité dans le service d'accès à la base de test**

La classe `ManagedDatabaseAccess` permet de manipuler les tables de tests. Les fixtures l'utilisent abondemment, et heureusement la plupart des opérations s'appuieront sur la reflexion pour spécifier les données. 

Néanmoins l'entité doit être indiquée dans `ENTITY_TYPES`. Le nom de la table devrait également être spécifié en constante dans cette classe.

Pour l'ajout dans `ENTITY_TYPES` ajouter à l'init static :

```
static {
    // ...
    // L'entier "9" permet de "trier" les tables pour les cascades de suppression
    ENTITY_TYPES.put(TTESTC, Pair.of(9, EfluidTestC.class));
}
``` 

**3/ Spécifier l'init de l'entrée dans le dictionnaire**

L'entrée dans le dictionnaire pour cette nouvelle table ne peut être deviné automatiquement. Il faut spécifier la config du dico à utiliser.

Pour cela, dans la méthode `fr.uem.efluid.system.common.SystemTest.initDefaultTables` ajouter un *CASE* supplémentaire pour la nouvelle table, comme pour cet exemple : 

```
   // ...
   case TTESTC:
       tables.add(table(DEFAULT_TTESTC, TTESTC, domain, "cur.\"COL1\"", DEFAULT_WHERE, "ID", STRING));
       break;
   // ...
```

> Attention en cas de link, regarder les autres *cases* pour voir comment le spécifier

**4/ Ajouter la table à l'init Efluid**

Une méthode d'init est réservée aux cas de tests Efluid. Il faut simplement indiquer que la nouvelle table doit être ajoutée dans le dico quand la base est initialisée dans un scénario.

Pour cela ajouter le nom de la table en argument dans l'appel de `initDictionaryForDefaultVersionWithTables` de `SystemTest`, ligne 234, comme ceci : 

```
initDictionaryForDefaultVersionWithTables(newDomain, newProject, TTEST1, TESTC);
```

**C'est tout pour le code java**

### Ecriture du scénario

Ajouter le nouveau scénario dans `param-app-cucumber\src\test\resources\gherkins\efluid-cases.feature`, en reprenant les exemples existants.

Les scénarios ont tous ce modèle pour valider un résultat de diff : 

```
  Scenario: Efluid merge 1
    Given the test is an Efluid standard scenario
    And the existing data in managed table "TTEST1" :
      | id        | col1           |
      | $testa_d  | testa delete   |
      | $testa_i1 | testa insert 1 |
      | $testa_u  | testa update 1 |
    And the commit ":tada: Test commit init" has been saved with all the identified initial diff content
    And these changes are applied to table "TTEST1" :
      | change | id        | col1             |
      | delete | $testa_d  |                  |
      | add    | $testa_i2 | testa insert 2   |
      | update | $testa_u  | testa update 1 2 |
    And a new commit ":construction: Update 1" has been saved with all the new identified diff content
    And the user has requested an export of the commit with name ":construction: Update 1"
    And the user accesses to the destination environment with the same dictionary
    And the existing data in managed table "TTEST1" in destination environment :
      | id        | col1           |
      | $testa_d  | testa delete   |
      | $testa_i1 | testa insert 1 |
      | $testa_u  | testa update 1 |
    And a commit ":construction: Destination commit initial" has been saved with all the new identified diff content in destination environment
    And a merge diff analysis has been started and completed with the available source package
    When the user access to diff commit page
    Then the merge commit content is rendered with these identified changes :
      | Table  | Key       | Action | Payload                                   |
      | TTEST1 | $testa_d  | REMOVE |                                           |
      | TTEST1 | $testa_u  | UPDATE | COL1:'testa update 1'=>'testa update 1 2' |
      | TTEST1 | $testa_i2 | ADD    | COL1:'testa insert 2'                     |
```

Les datatables permettent donc ici de spécifier les données de test.

> Attention aux noms des colonnes, bien reprendre les fields de l'entité Jpa qui a été ajoutée pour la table dans les `the existing data in managed table "TTEST1" :`

Le nom du scénario doit être unique

Les opérations de changes nécessite en 1ère colonne "`change`" une des trois valeur exacte : `delete` / `add` / `update`

Le contenu du diff pour le `Then` de validation doit reprendre le format "HR" (Human Readable) présenté à l'écran dans DataGate. L'`Action` est un code interne de DataGate : `REMOVE` / `ADD` / `UPDATE`.

> Attention à la case.

Le payload utilise bien le nom des colonnes en BDD, pas le nom des fields. JPA converti tout seul les fields camelCase en SnakeCase : `myValue` devient `MY_VALUE`

## Utilisation du scénario

Avec intellij le run / debug depuis un scénario (clic droit dans le gherkin, au niveau du scénario) est fonctionnel.

> A noter que le traitement peut être assez long : le scénario standard monte une BDD de test + initialise le dico (version, projet et domaine y compris)
 lance un diff, attend la fin du diff, le commit avec tout selectionné, l'export en .par, reset l'index pour simuler 
 "la destionation", réinit les données de test avec diff puis commit initial, importe réellement le .par, 
 et enfin lance le diff de merge

