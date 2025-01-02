pipeline {
    agent any

    environment {
        AWS_REGION      = 'us-east-2'
        IMAGE_NAME      = 'wallet-service'
        ECR_REGISTRY    = 'public.ecr.aws/z1z0w2y6'
        DOCKER_BUILD_NUMBER = "${BUILD_NUMBER}" //Docker image tag based on jenkins build number
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

       stage('Build Docker Image') {
            steps {
                script {

                      sh """
                           aws ecr-public get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                        """
                    // Build and tag the Docker image
                     sh """
                         docker build -t ${IMAGE_NAME}:${DOCKER_BUILD_NUMBER} .
                         docker tag ${IMAGE_NAME}:${DOCKER_BUILD_NUMBER} ${ECR_REGISTRY}/${IMAGE_NAME}:${DOCKER_BUILD_NUMBER}
                    """

                }
            }
        }
        stage('Push to ECR') {
            steps {
                script {
                    sh "docker push ${ECR_REGISTRY}/${IMAGE_NAME}:${DOCKER_BUILD_NUMBER}"
                }
            }
        }
    }

    post {
        always {
            cleanWs()
              sh """
                docker rmi ${ECR_REGISTRY}/${IMAGE_NAME}:${DOCKER_BUILD_NUMBER} || true
                docker rmi ${IMAGE_NAME}:${DOCKER_BUILD_NUMBER} || true
            """
        }
    }
}