# Plugin maven de génération de dictionnaire à partir de l'API

## API de spécification de dictionnaire

Voir le détail dans l'article [API de spécification](spec-api.md)

Une API permettant de spécifier un dictionnaire complet (table, domaines et liens) est fournie dans le module **efluid-datagate-api**

Voici un exemple de mise en oeuvre : 
    
    @GestionDuMateriel
    @ParameterTable(name = "Categorie", tableName = "TCATEGORY")
    public class CategorieDeMateriel {
       
       private Long id;
       
       @ParameterValue("NAME")
       private String name;
       
       @ParameterKey("CODE")
       private String code;
       
       // ...
    }

## Utilisation du plugin maven
L'API est utilisée par un générateur **(plugin maven)** dédié spécifié dans le module `efluid-datagate-generator`. 

**Celui ci est mis en oeuvre avec la configuration suivante :** 
```
<plugin>
    <groupId>${project.groupId}</groupId>
    <artifactId>efluid-datagate-generator</artifactId>
    <executions>
       <execution>
          <id>generate</id>
          <phase>generate-sources</phase>
          <goals>
          	   <goal>generate</goal>
          </goals>
       </execution>
    </executions>
    <configuration>
       <destinationFileDesignation>generated-dictionary</destinationFileDesignation>
       <destinationFolder>${project.basedir}/target</destinationFolder>
       <protectColumn>true</protectColumn>
       <sourcePackage>fr.uem.efluid.sample</sourcePackage>
       <uploadToServer>true</uploadToServer>
       <uploadEntryPointUri>http://127.0.0.1:8080/rest/v1/dictionary</uploadEntryPointUri>
    </configuration>
</plugin>
```
Ce générateur construit un fichier .par d'export / import de dictionnaire, immédiatement importable dans une instance de l'application. Il est également capable de l'uploader directement sur une instance active de l'application.

**Les propriétés de configuration sont :** 
* `destinationFileDesignation` : identifiant du nom de l'archive .par. Si égale à "auto", alors un uuid aléatoire est utilisé.
* `destinationFolder` : Emplacement de sortie du fichier .par généré. Par défaut "${project.basedir}/target"
* `protectColumn` : Indique si le format de colonne de la BDD utilisée doit être protégé (identique option équivalente de l'application)
* `sourcePackage` : Racine du package java à parser pour la recherche du modèle du dictionnaire
* `uploadToServer` : Si true, va uploader le .par dans une instance de l'application. Par défaut false
* `uploadEntryPointUri` : Url du point d'entrée du service REST "dictionnaire" de l'application où le .par sera uploadé. Exemple : "http://127.0.0.1:8080/rest/v1/dictionary"

## Projet exemple

Un projet exemple sur l'utilisation du plugin et de l'api est fourni : **efluid-datagate-generator-example**, avec un modèle complet. 

Un script SQL (oracle) d'initialisation des tables correspondantes est fourni dans src/database
