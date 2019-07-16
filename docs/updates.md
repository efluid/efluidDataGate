# Mise à jour de l'application datagate

L'application est fournie sous la forme d'un jar executable. Utiliser une nouvelle version signifie executer une nouvelle version du jar.

Comme une base de données est nécessaire, l'application se charge de mettre à jour si nécessaire la base de donnée de fonctionnement si elle a été initialisée avec une version antérieur

## Mise à jour de la base de données

La solution technique utilisée pour la mise à jour de la BDD est Flyway, en version communautaire.

Seules les BDD suivantes sont supportées :
* Oracle 12.2 et plus
* Postgres
* H2

Attention, la version Oracle Express Dockerisée disponible avec l'image `sath89/oracle-12c` **n'est pas supportée**. La mise à jour doit être alors manuelle, ou il faut démarrer l'application avec un DDL HBM auto adapté

## Retour en arrière

Aucun dispositif de retour en arrière n'a été prévu. Démarrer une ancienne version sur une base de données de fonctionnement plus récente est à vos risques et périles