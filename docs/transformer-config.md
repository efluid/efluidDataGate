# Les transformers disponibles et leur configuration

## Principes communs sur la configuration des transformers

La configuration est spécifiée en format json. Le format du json utilisé dépend du type de transformateur : ils sont tous différents.

L'éditeur de json donné dans l'écran de spécification des transformateurs est simpliste et ne vérifie pas le contenu à la volée. 
Néanmoins les transformateurs embarquent un dispositif de vérification de configuration, pour s'assurer qu'elle est valide. L'enregistrement est donc bloqué tant que la configuration est incorrecte.
