# Docker build pour Paramethor

Dans le dossier courant sont gérées les variantes de packaging docker pour le projet

Un dossier par type de build : 

* build-desktop : un type de build simple fonctionnel sur un poste de dev linux ou windows 10 pour créer un container simple
* build-serv-efluid : un build tournant sur un serveur de Efluid avec besoin de proxy
* build-struff : les versions spécifiques pour tester avec Struff

Pour utiliser le build sur un poste de dev windows, par exemple pour la version avec postgres embarqué : 

    ## être dans le dossier racine du projet
    cd GestionParamEfluid
    ## lancer le script de build depuis le dossier racine
    ./param-app/src/docker/build-desktop/standalone-with-postgres/build.ps1

Pour builder une version avec H2 sur un serveur efluid :

    ## être dans le dossier racine du projet
    cd GestionParamEfluid
    ## lancer le script de build depuis le dossier racine
    ./param-app/src/docker/build-serv-efluid/standalone-with-h2/build.sh