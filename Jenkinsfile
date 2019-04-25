pipeline {
    agent any
    stages {
        stage('Build') {
            agent {
                docker {
                    image 'maven:3.6.1-jdk-11-slim'
                    args '-v /root/.m2:/root/.m2'
                }
            }
            steps {
                sh '''
                   sed -i 's|localhost|database|' src/main/resources/application-dev.yml src/test/resources/application-test.yml
                   sed -i 's|useSSL=false|&\\&allowPublicKeyRetrieval=true|' src/main/resources/application-dev.yml src/test/resources/application-test.yml
                   sed -i 's|password: .*$|password: sandwich|' src/main/resources/application-dev.yml src/test/resources/application-test.yml
                   sed -i 's|user: .*$|user: root|' src/test/resources/application-test.yml
                   sed -i 's|.allowedOrigins("http://localhost:3000")||' src/main/java/com/list/todo/configurations/SecurityConfiguration.java
                   sed -i 's|localhost|todo-front|' src/main/java/com/list/todo/configurations/WebSocketConfig.java
                   '''
                sh 'mvn -B -DskipTests clean package'
                archiveArtifacts 'target/*.jar'
            }
        }
        stage('Push') {
            steps {
                script {
                    env.NUM_BUILD = env.BUILD_NUMBER
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
