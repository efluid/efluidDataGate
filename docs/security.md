# Gestion de la sécurité

Utilisation de [pac4j](http://www.pac4j.org/) "seul" (sans spring-security). La configuration technique des composants de sécurité est faite dans *fr.uem.efluid.config.SecurityConfig*

Deux *clients* pac4j avec 2 chaines d'authentification / interception :
* Les urls "/ui/*" (écrans de l'application) sont sécurisés avec une authentification "web" classique (login), avec compte en session.
* Les urls "/rest/*" (services rest) sont sécurisés avec une récupération de token privé propre à chaque user.

Attention, ce n'est pas supposé être "web-proof" (mais l'application n'est pas supposée l'être du tout : on parle là d'un outil d'administration de données directement en BDD, un peu comme l'est Oracle SQL Developer)

Il n'y a pas de rôle géré (pour l'ajouter : créer données en BDD et les utiliser dans les checks réalisés dans fr.uem.efluid.security.AllAuthorizer). Le mot de passe est encodé en SHA-256 (hash simple, pouvant être amélioré - ajout de salt par exemple - : géré dans fr.uem.efluid.security.ShaPasswordEncoder).
