# Configurer la gestion de merge par "spécification de cas"

## Principes

Le merge est implémenté avec 2 approches différentes :

* Une approche "générale" basée sur un algo recherchant une approche adaptée en fonction des modifications importées et locale. C'est la solution initialement en place, des bugs sont connus (et pourraient être corrigés)
* Une approche "systématique" basée sur la configuration de règles à appliquer en fonction de différents critères. Tous les cas doivent être anticipés et configurés à l'avance. 

L'approche systématique nécessite une configuration poussée des cas de merge, spécifiés dans le fichier dédié `merge-resolution-rules.json`

> Actuellement l'application est configurée pour utiliser l'approche "systématique"

## Scénario utilisé pour la nouvelle approche "systématique"

Le processus est le suivant :

Quand un merge est réalisé, on commence par identifier le timestamp du moment où le 1er commit du merge a été réalisé.

A partir de ce timestamp, le traitement suivant est lancé :

* Récupération dans la destination de l'index de tous les changements réalisés avant ou au moment du timestamp
* Reconstitution des données telles qu'elles étaient à ce moment, à partir de cet index. C'est l'état des donnés "previous"
* Récupération de l'état des donnés actuelles de la base destination. C'est l'état des données "actual"
* Génération du diff entre "previous" et "actual". C'est le diff "mine" = le diff des changements en conflits potentiels avec celles importées
* Le diff importé est appelé diff "their" 
* Pour chaque clé concernée par "their" et/ou "diff", on lance alors le traitement de merge

Les cas de merge sont alors identifiés par les critères suivants :

* Type de changement dans le diff "their". Peut être `ADD`, `REMOVE`, `UPDATE` ou `null` (null = pas d'entrée de diff "their" pour la clé concernée)
* Type de changement dans le diff "mine". Peut être `ADD`, `REMOVE`, `UPDATE` ou `null` (null = pas d'entrée de diff "mine" pour la clé concernée)
* Résultat de la comparaison des payloads entre "mine" et "their". Peut être `SIMILAR` ou `DIFFERENT`. Prend en compte la nullité (donc si mine est null, et their non null, alors c'est `DIFFERENT`. Mais si mine et their sont null tous les deux, alors c'est `SIMILAR`) 

Avec ce traitement on applique alors directement la règle spécifiée dans la config. 

> Le nom de la règle est par ailleurs enregistré dans le diff entry. Il est possible de l'afficher, ou de l'utiliser dans les tests par exemple 

## Configuration des cas de merge

La configuration dans le fichier JSON consiste à décrire tous les cas possibles, en indiquant pour chaque combinaison des 3 critères, la résolution à appliquer. Il y a 4 valeurs possibles pour 2 critères plus 2 valeurs pour le dernier, soit maximum 4 * 4 * 2 = 32 combinaisons maximum.

> Même si certaines peuvent paraitre "impossibles", avec la gestion du "cherry pick" ce n'est pas le cas. On peut en effet importer en cherry pick un commit qui supprime par exemple une ligne que nous n'avons jamais importé

Le fichier JSON est au format suivant : 

```
[
  {
    "caseName": "REMOVE mine - ADD their",
    "their": "ADD",
    "mine": "REMOVE",
    "payload": "DIFFERENT",
    "resolution": {
      "payload": "THEIR_PAYLOAD",
      "action": "ADD",
      "needAction": true
    }
  },
  ...
]
```

Les informations d'identification sont donc : 

* **caseName** : un nom unique informatif sur le cas spécifié
* **their** : l'action pour le diff their (`ADD`, `REMOVE`, `UPDATE` ou `null`). Si null, il n'y a donc pas de their
* **mine** : l'action pour le diff mine (`ADD`, `REMOVE`, `UPDATE` ou `null`). Si null, il n'y a donc pas de their
* **payload** : `SIMILAR` ou `DIFFERENT`
* **resolution** : la résolution à réaliser pour le merge

Les informations de résolution sont :

* **payload** : `THEIR_PAYLOAD` => on utilise le payload du diff "their", `MINE_PAYLOAD` => on utilise le payload de "mine", `null` => Payload null (pour REMOVE par exemple)
* **action** : `ADD`, `REMOVE` ou `UPDATE` => L'action finale. Doit correspondre à un changement par rapport au "actual" si on souhaite faire résoudre le changement (donc si on gère un cas où actual)
* **needAction** : `true` / `false` => indique si une opération de validation du changement sera présentée à l'utilisateur. Si `false`, la modification est considérée "déjà en place" (par exemple quand "mine" et "their" ont réalisé la même opération)  

Si un cas n'est pas traité lors du merge, l'erreur `Unsupported merge case resolution` est remontée