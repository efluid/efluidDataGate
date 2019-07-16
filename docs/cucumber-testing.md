# Tests systèmes avec Cucumber

## Approche BDD en place

L'application a été développée en mode proto technique, code first. Les fonctionnalités n'ont été spécifiés qu'à postériori, sous la forme de Gerhkins

## Lancer les tests 

### Depuis eclipse (Lancement manuel)

Les tests cucumber sont des tests Junit. Il y a une seule classe point d'entrée pour tous les tests : ``/datagate-app-cucumber/src/test/java/fr/uem/efluid/system/tests/AllCucumberTest.java``. Le runner est bien géré par le plugin Junit de eclipse et permet de suivre l'avancement.

### Au build

Les tests sont exécutés automatiquement lors du build maven

## Compléter les tests

### Fichiers Gherkins

Les behaviors sont spécifiées en syntaxe Gherkin dans le dossier ``/datagate-app-cucumber/src/test/resources/gherkins``. Il y a un fichier .feature par ensemble de fonctionnalité

Exemple de syntaxe : 

    Feature: The dictionary is specified in functional domains
  
    Scenario: A functional domain edit page is available to all users
      Given from the home page
      When the user access to functional domain edit page
      Then the provided template is functional domain

Quelques conseils :

* Tout d'habord, bien maitriser les principes de Gherkin : https://docs.cucumber.io/gherkin/reference/
* L'indentation est importante
* Respecter les règles de casse : Majuscule au début des mots clés
* A priori Background n'est pas pris en compte par Cucumber java
* Ne pas oublier que nous avons le droit à l'implicite : pas besoin de dire que l'application est installée, configurée, avec telle ou telle donnée, que l'utilisateur est authentifié si de toute façon il n'est pas possible d'aller sur une page concernée avant ça : on ne test les éléments que par Behavior, on ne reproduit pas tout le scénario complet de toutes les behaviors à chaque fois


