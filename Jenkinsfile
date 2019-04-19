pipeline {
    agent any
    stages {
        stage('Build') {
            agent {
                docker {
                    image 'maven:3-alpine'
                }
            }
            steps {
                sh '''
                   sed -i 's|localhost|database|' src/main/resources/application-dev.yml
                   sed -i 's|useSSL=false|&\\&allowPublicKeyRetrieval=true|' src/main/resources/application-dev.yml
                   sed -i 's|password: .*$|password: sandwich|' src/main/resources/application-dev.yml
                   '''
                sh 'mvn -B -DskipTests clean package'
                archiveArtifacts 'target/*.jar'
            }
        }
        stage('Push') {
            steps {
                script {
                    def ECR_REPO = "618548633277.dkr.ecr.eu-west-1.amazonaws.com/todolist-app"
                    unarchive(mapping: ['target/*.jar' : '.'])
                    docker.withRegistry('https://' + ECR_REPO, 'ecr:eu-west-1:aws_ecr_creds') {
                        docker.build(ECR_REPO + ':${BUILD_NUMBER}').push()
                    }
                }
            }
        }
    }
}
