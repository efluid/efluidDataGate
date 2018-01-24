# Gestion du paramètrage Efluid

Prototype d'application dédiée à l'identification, au packaging et au déploiement de paramètrage pour une instance Efluid.

## Utilisation

Le prototype est basé sur Spring-boot. Il n'y a rien à installer pour l'exécuter _par défaut_ :
* Il démarre dans un mode "demo" par défaut
* Il utilise alors une BDD embarquée H2 auto-générée. Attention elle est dropée au moment du stop

Le fichier de configuration technique de l'application est src/main/resources/application.yml

### Quickstart
Pour démarrer sans rien installer, juste à partir du projet cloné, utiliser : 

    mvn spring-boot:run
    
L'application démarre après build. Le service est accessible à l'adresse [http://localhost:8080](http://localhost:8080)

### Démarrage depuis un IDE
Pour démarrer depuis un IDE, lancer la classe exécutable __fr.uem.efluid.Application__

### Templating
Les templates sont traités avec Thymeleaf. Ils restent des HTML valides. Ils sont éditables à chaud (tant que le cache de template est désactivé dans application.yml)