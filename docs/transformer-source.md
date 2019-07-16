# Sources de transformation

La notion de "transformation" (terme technique "transformer") a été intégrée dans DataGate pour gérer les usages de la régionalisation de Efluid.

Le besoin fonctionnel est très "large" et peut inclure différents usages, mais pour résumer :

* Les données d'une instance Efluid Source peuvent être adaptées spécifiquement sur certaines destination
* L'adaptation passe par la recherche de valeurs à substituer, et par l'application de nouvelles valeur
* Les données de substitution viennent généralement d'une table, mais pas toujours
* Quand elles viennent d'une table, cette dernière est très volumineuse, et ne fait pas partie du paramètrage. Seule une petite partie de ses données sont nécessaires à chaque modification de paramètrage

Datagate fourni en conséquence de quoi spécifier des transformations de données, d'éventuelles tables sources, et va se charger d'empacter les données nécessaires pour transformer le contenu d'un lot. Pour détecter les données nécessaires et faire les substitutions, un composant spécifique (extension de type "transformer") sera utilisé.

## Types de transformations 

Les transformations gérées dans Datagate sont de 2 types : 

* Transformation "simple": le composant extension est alors autonome, il n'utilise pas de données d'une table source. Il détect simplement si un payload de mis à jour doit être transformé lors d'un import, et applique la modification si nécessaire
* Transformation "avec source" : le composant utilise cette fois des données de référence issue de la BDD managée source. Il fourni, en plus des fonctions de transformations, des fonctions pour identifier les données de source qui doivent être embarqués avec un lot exporté.

Le dictionnaire DataGate permet de spécifier facilement la table de source utilisée, et de l'associer à une transformation.

## Spécification de source de transformation

Dans l'édition de dictionnaire, depuis le menu haut, choisir "Sources de transformation"

Plusieurs sources peuvent être créées. 

Elles comportent : 
* Un nom de table de source
* Un composant de transformation à appliquer.
* Un paramètrage pour le composant

La liste des composants de transformation est issue de la clé de paramètrage `datagate-efluid.transformers.sourced-transformers`

Le détail sur le fonctionnement même des transformers est donné dans le chapitre [Transformation de données](transformer-apply.md)

Le composant étant potentiellement très spécifique, le paramètrage est "libre" : c'est un champ texte, le composant doit se charger d'interpréter le paramètrage.