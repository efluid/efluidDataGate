import com.efluid.jenkins.*

JenkinsUtils jenkinsUtils = new JenkinsUtils(this)

def body = {
    def credentialsId = env.credentialsId
    git branch: env.BRANCH_NAME, credentialsId: credentialsId, url: 'git@github.com:efluid/efluidDataGate.git'

    try {
        def adressMailFrom = env.adressMailFrom
        def adressMailTo = env.adressMailTo
        sh 'mvn clean install'
        junit allowEmptyResults: true, testResults: '**/surefire-reports/*.xml,**/failsafe-reports/*.xml'
    } catch (Exception e) {
        mail bcc: '', body: """Datagate en erreur

	${env.BUILD_URL}""", cc: '', from: adressMailFrom, replyTo: '', subject: 'Test datagate', to: adressMailTo
        throw e
    }
}

if (jenkinsUtils.isCjeProd()) {
    node('socle-jenkins-maven-docker-14-4G') {
        body.call()
    }
} else {
    new EfluidPodTemplate(this).addContainer(new Container(this).containerType("maven-14").memory("4G")).execute() {
        container("maven-14") {
            body.call()
        }
    }
}

return this;