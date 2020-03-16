# Gestion de versions

Les versions sont des "TAGS" sur le dictionnaire. Elles spécifient un état à un instant donné pour le dictionnaire, pour lequel des opérations de préparation de lot peuvent être effectuées en toute confience.

La préparation de lot ne peut être lancé que si une version valide est définie. Si le dictionnaire est modifié sans qu'une version ait été créée ou mise à jour, alors la préparation n'est possible

## Création des versions

Les versions sont spécifiées dans l'écran dédié du dictionnaire.

Une version est simplement identifiée par son **nom** qui peut être :
* Une valeur libre si la `feature` `USE_MODEL_ID_AS_VERSION_NAME` est désactivée
* L'ID courant du modèle si la `feature` `USE_MODEL_ID_AS_VERSION_NAME` est activée

Les versions sont alors associées à :
* Leur date de mise à jour. On peut mettre à jour une version "courante" pour représenter la dernier état valide du dictionnaire
* L'utilisateur créateur
* L'id courant du modèle de la base managée (suivant le type de `ModelIdentifier` activé)
* Une "photo" du dictionnaire courant (pour permettre des comparaisons entre versions)

## Mise à jour de version

En mettant à jour une version on rafraichit l'état du dictionnaire en "photo" et la date de mise à jour. Tant qu'aucun lot n'a été créé avec la version, elle peut être mise à jour.

## Comparaisons entre versions

Les versions peuvent être comparées par rapport à la dernière version active : les différences au niveau du dictionnaire entre les 2 versions sont alors affichées

