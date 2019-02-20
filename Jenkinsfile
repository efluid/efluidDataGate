import com.efluid.jenkinsfile.Jenkinsfile

// variables globales
scriptParams = [:]
scriptParams['scriptPath'] = 'groovy/workflow/application/build/Jenkinsfile.groovy'
activeNode = false
scriptParams['application'] = 'datagate'
scriptParams['applicationVersion'] = '0.2.1-SNAPSHOT'
scriptParams['nodeLabel'] = params['nodeLabel'] != null ? params['nodeLabel'] : env.nodeLabel
scriptParams['nodeLabel'] = scriptParams['nodeLabel'] != null ? scriptParams['nodeLabel'] : 'socle-jenkins-maven-docker-14-4G'
scriptParams['destinatairesMail'] = params['destinatairesMail'] != null ? params['destinatairesMail'] : env.destinatairesMail
scriptParams['destinatairesMail'] = scriptParams['destinatairesMail'] != null ? scriptParams['destinatairesMail'] : 'usinelogicielle@efluid.fr'
scriptParams['skipAllocationNodeInJenkinsfile'] = params['skipAllocationNodeInJenkinsfile'] != null ? params['skipAllocationNodeInJenkinsfile'] : env.skipAllocationNodeInJenkinsfile
scriptParams['forceJobExec'] = false

new Jenkinsfile(this, scriptParams).runJenkinsfile()

return this;