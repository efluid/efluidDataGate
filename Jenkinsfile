import com.efluid.jenkinsfile.Jenkinsfile

// variables globales
node('socle-jenkins-maven-docker-14-4G'){
def credentialsId = env.credentialsId
git branch: env.BRANCH_NAME, credentialsId: credentialsId, url: 'https://github.com/Zenika/GestionParamEfluid.git'

try{
	def adressMailFrom = env.adressMailFrom
	def adressMailTo = env.adressMailTo
	sh 'mvn clean install'
	junit allowEmptyResults: true, testResults: '**/surefire-reports/*.xml,**/failsafe-reports/*.xml'
} catch (Exception e) {
	mail bcc: '', body: """GestionParamEfluid en erreur 

	${env.BUILD_URL}""", cc: '', from: adressMailFrom, replyTo: '', subject: 'Test GestionParamEfluid', to: adressMailTo
	throw e
}
}
return this;