# Lire le contenu des fichiers d'export

## Principes

Les fichiers .par exportés par l'application peuvent être lus assez facilement. Ce sont des fichiers zip, contenant plusieurs fichier textes ".packs" (1 fichier par type de données exporté) + pour l'export de commit des fichiers annexes. Le nom du fichier .packs précise le type de données.

Les fichiers .packs sont des exports "flat" des données, structurés ainsi :

 * Un entête donne du détail sur le type d'entité technique associé :
```
    [pack|fr.uem.efluid.services.types.DictionaryPackage|full-dictionary|2019-04-05T12:36:26.516426100|2]
       |                                      |                  |                      |              |
       +-> Début de pack. Fini [/pack]        |                  +-> Type d'export      |              |
                                              +--> Classe de l'entité exportée          +-> Date exp.  |
                  La version spécifiée dans la classe de l'entité exportée. Vérifiée à l'import <------+
```
 * Un contenu "grappe d'entité par grappe d'entité". la présence d'une vrai grappe avec des entités enfant est en réalité limitée à l'export des commits, qui contiennent commit + index. En général il y a donc une entité par ligne, soit une ligne de table de gestion par ligne de pack. Les lignes sont spécifiées avec `[item] ... [/item]`
 * Les items sont en réalité du json "sur une seule ligne" avec des attributs "courts" sur trois lettres. Par exemple en formatant l'item d'un fichier .packs de dictionnaire on obtiendrait : 
 ```
 [item]
 {
    "uid":"aa6571db-2369-4655-96a7-75eef5fbb89c",
    "kty":"PK_ATOMIC",
    "dom":"5e84c0d3-ab5b-4571-bdc0-4f43d6820749",
    "tab":"T_RECETTE_COMPOSITE",
    "whe":"1=1",
    "cre":"2019-04-05 12:34:22",
    "upd":"2019-04-05 12:34:22",
    "k1n":"ID_TWO",
    "nam":"Composite simple",
    "sel":"cur.\"DETAILS\", cur.\"VALUE\"",
    "kna":"ID_ONE",
    "k1t":"PK_ATOMIC"
}
[/item]
```
 * Les valeurs null sont omises de l'export

**Il est donc possible de retrouver manuellement les informations en dézippant un par et en consultant les fichiers .packs**

Voici les correspondances des attributs "courts" pour les principales données de l'application :

**Dictionnaire**

* Fichier *"full-dictionary.packs"*
* Contenu : Entrée de dictionnaire
* *Valeurs* :
  * **uid** => Uuid de l'entrée de dico
  * **nam** => Nom de l'entrée (= nom du paramètre)
  * **tab** => Nom de la table en BDD
  * **cre** => Date de création
  * **upd** => Date de MAJ
  * **dom** => UUID du domaine fonctionnel
  * **kna** => Nom (colonne) de la clé principale
  * **kty** => Type (interne) de la clé principale 
  * de **k1n** à **k4n** => Nom (colonne) de la clé d'extension 1 à 4 (dans le cas des composites key)
  * de **k1t** à **k4t** => Type (interne) de la clé d'extension 1 à 4 (dans le cas des composites key)
  * **sel** => Critère de sélection (clause de sélection des colonnes de valeurs)
  * **whe** => Clause de filtrage spécifique


A compléter



 