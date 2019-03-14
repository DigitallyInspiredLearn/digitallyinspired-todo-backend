pipeline {
    agent any
    stages {
        stage('Build') {
            agent {
                docker {
                    image 'maven:3-alpine'
                    args '-v /root/.m2:/root/.m2'
                }
            }
            steps {
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
