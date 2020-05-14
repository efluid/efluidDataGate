# Import de lots

> Rédaction en cours

## Principe 

## Synthèse du traitement

2 Grands temps : 
1. l'import des lots (extraction de toutes les données préparatoire au merge) => Synchrone
2. Le traitement du merge (identification des données "à garder / proposer" pour commit local) => Asynchrone

### Focus sur le traitement d'import

> Traitement initialisé par `fr.uem.efluid.services.CommitService#importCommits`

1. Lecture du fichier `.par` : extraction des données
2. Validation du contenu du `.par` et chargement en mémoire de son contenu
3. Récupération des commits déjà présents sur l'instance courante
4. Traitement uniquement sur les commits non encore importés, 1 par 1 :
  * Processus de vérification du dictionnaire référencé par les commits : 
     * Vérification (optionnelle, activée avec `datagate-efluid.imports.check-model-version`) que toutes les versions de dictionnaire référencées par les commits sont présentes dans la destination à l'identique
     * Vérification (optionnelle, activée avec `datagate-efluid.imports.check-dictionary-compatibility`) que le dictionnaire de la destination est compatible avec les données importées. Les versions peuvent être différentes.
  * Garde le commit valide pour traitement asynchrone
5. Arrêt si une erreur a été identifiée
6. Sinon finalisation de la préparation des données pour le merge

### Focus sur le merge

> Traitement initialisé par `fr.uem.efluid.services.PilotableCommitPreparationService#processAllMergeDiff` et plus précisément traitement par table de paramètrage dans `fr.uem.efluid.services.PrepareIndexService#completeMergeIndexDiff`

Pour chaque table de paramètrage, pour l'index combiné des commits importés :

1. Chargement de l'index "précédent" 
2. Reconstruction des données "connues" à partir de l'index "précédent"
3. Chargement des données actuellement réellement présentes dans la table de paramètrage
4. Préparation d'un diff entre le connu et le réellement présent
5. Transformation des données importées (si des transformateurs sont associés aux lots importés)
6. Diff de merge à partir des données importés (transformées), de l'actuellement présent et du connu. Ce diff est traité par un processus par use case, à partir d'une configuration de résolution de chaque possible
7. Finalisation des données pour un rendu utilisateur cohérent
