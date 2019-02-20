import com.efluid.jenkinsfile.Jenkinsfile

// variables globales
node('socle-jenkins-maven-docker-14-4G'){

//checkout([$class: 'GitSCM', branches: [[name: '*/efluid_test']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'd2d9d156-0580-498f-8135-64aad97156cb', url: 'git@github.com:Zenika/GestionParamEfluid.git']]])
git branch: env.BRANCH_NAME, credentialsId: 'd2d9d156-0580-498f-8135-64aad97156cb', url: 'git@github.com:Zenika/GestionParamEfluid.git'

sh 'mvn clean install'

}
return this;