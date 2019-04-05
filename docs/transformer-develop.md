# Extension : transformers

Les transformers sont des composants pouvant être développés spécifiquement pour répondre à des besoins métiers. Ils traitent les aspects "régionalisation" de Efluid en particuliers, et autres transformations de données lors de l'import.

## Transformer Direct : 

Transformer simple sans données source. Implémentent `fr.uem.efluid.tools.DirectTransformer`

## Transformer Sourcé : 

Traitement avec source de données partielle incluse lors de l'import. Implémentent `fr.uem.efluid.tools.SourcedTransformer`
