import com.efluid.jenkinsfile.Jenkinsfile

// variables globales
node('socle-jenkins-maven-docker-14-4G'){

//checkout([$class: 'GitSCM', branches: [[name: '*/efluid_test']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'd2d9d156-0580-498f-8135-64aad97156cb', url: 'git@github.com:Zenika/GestionParamEfluid.git']]])
git branch: env.BRANCH_NAME, credentialsId: '4adbeffc-4d68-43a1-b44e-d3de9ceace04', url: 'https://github.com/Zenika/GestionParamEfluid.git'

try{
	sh 'mvn clean install'
	junit allowEmptyResults: true, testResults: '**/surefire-reports/*.xml,**/failsafe-reports/*.xml'
} catch (Exception e) {
	mail bcc: '', body: """GestionParamEfluid en erreur 

	${env.BUILD_URL}""", cc: '', from: 'usinelogicielle@efluid.fr', replyTo: '', subject: 'Test GestionParamEfluid', to: 'm-coste@efluid.fr'
	throw e
}
}
return this;