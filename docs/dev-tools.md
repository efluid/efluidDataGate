# Outils de développement à mettre en place

> Le projet a été développé sur Eclipse et IntelliJ. L'IDE de référence est désormais uniquement IntelliJ

## Java / Maven

Testé avec JDK11

## Oracle

Il est nécessaire d'avoir une BDD Oracle de dispo comme source. Oracle express ou version docker (donc avoir docker en local)

Installer SQLDeveloper

## Utilisation de IntelliJ

* Installation d'une version communautaire 2019.1 (pas testé avec version ultimate)
* Plugins :
  * Sélection de plugins recommandés :
    * Build tools -> Maven
    * Version controls -> Git / Github
    * Test tools -> Junit / Coverage
    * Other tools -> tous
    * Tout le reste désactivé par défaut

* Checkout VCS (plus simple) -> Github

* Dans settings > Plugins -> Ajouter Cucumber for java (+ OK pour installer plugins liés)

* Clic droit sur la classe fr.uem.efluid.Application > Create Run/Debug config et ajouter en "Program Arguments" : `--spring.config.location=classpath:/application.yml,classpath:/config/application.yml`

