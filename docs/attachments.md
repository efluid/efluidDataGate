# Ajout de scripts à exécuter dans les commits

## Présentation de la fonctionnalité

Les commits peuvent être accompagnés de pièces jointes. Les formats de pièce jointes sont reconnues et si elles sont de type SQL, elles peuvent être exécutées avec le contenu du commit lors d'un merge.

L'objectif est de permettre de joindre des scripts correctifs de données aux paramétrages.

## Fonctionnement de la spécification de PJ

Le formulaire de saisie de lot (/efluid-datagate-app/src/main/resources/templates/pages/commit.html) intègre une zone d'upload de PJ.

Les PJ sont traitées par ``fr.uem.efluid.services.PilotableCommitPreparationService.addAttachmentOnCurrentCommitPreparation(ExportFile)`` et stockées comme fichier temporaire par le code suivant :

	// Store attachment into a tmp file
	Path path = SharedOutputInputUtils.initTmpFile(ATTACH_FILE_ID, ATTACH_EXT, true);
	Files.write(path, file.getData());  
	
Les fichiers d'attachments sont identifiés par leur nom de fichier (qui doit donc être unique dans un commit), et spécifiés dans les données de la ``fr.uem.efluid.services.types.PilotedCommitPreparation<?>`` courante. Comme cela ils peuvent être présentés en liste dans la page d'upload

Lors de la sauvegarde du commit, le chemin de référence des fichiers temporaires est traité, de même que le nom du fichier pour récupérer et stocker sont contenu.

Le traitement est réalisé par ``fr.uem.efluid.services.CommitService.saveAndApplyPreparedCommit(PilotedCommitPreparation<A>)``, et plus précisément dans ``fr.uem.efluid.services.CommitService.prepareAttachments(Collection<AttachmentLine>, Commit)``. Ils sont stockés en BDD comme ``fr.uem.efluid.model.entities.Attachment``

## Reconnaissance du type

A ce stade c'est une simple reconnaissance par le nom du fichier. Une véritable détection de contenu serait nécessaire.

Le traitement de reconnaissance est réalisé par ``fr.uem.efluid.model.entities.AttachmentType.fromContentTypeAndFileName(String, String)`` pour obtenir un fr.uem.efluid.model.entities.AttachmentType

Les formats supportés sont :

* MD_FILE - ".md"
* SQL_FILE - ".sql" Format exécutable de script SQL
* PAR_FILE - ".par" Format archive de l'application. Il a été envisagé de le rendre exécutable (import de dictionnaire depuis import de lots par exemple)
* ZIP_FILE - ".zip" 
* TEXT_FILE - ".txt"
* OTHER - tout le reste

## Lecture du contenu des PJ

Les PJ peuvent être affichées directement dans la page d'upload ou lors de 

Les ``fr.uem.efluid.model.entities.Attachment`` sont exportables

TODO : A compléter