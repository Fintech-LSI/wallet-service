pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-2'
        IMAGE_NAME = 'wallet-service'
        ECR_REGISTRY = 'public.ecr.aws/z1z0w2y6'
        // Add AWS credentials environment variable
        AWS_CREDENTIALS = credentials('aws-credentials')
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
                    // Get the ECR token and store it in a variable
                    def ecrLogin = sh(
                        script: "aws ecr-public get-login-password --region ${AWS_REGION}",
                        returnStdout: true
                    ).trim()

                    // Use the token directly with docker login
                    sh """
                        echo '${ecrLogin}' | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                        docker build -t ${IMAGE_NAME}:latest .
                        docker tag ${IMAGE_NAME}:latest ${ECR_REGISTRY}/${IMAGE_NAME}:latest
                    """
                }
            }
        }

        stage('Push to ECR') {
            steps {
                script {
                    sh "docker push ${ECR_REGISTRY}/${IMAGE_NAME}:latest"
                }
            }
        }
    }

    post {
        always {
            cleanWs()
            sh """
                docker rmi ${ECR_REGISTRY}/${IMAGE_NAME}:latest || true
                docker rmi ${IMAGE_NAME}:latest || true
            """
        }
    }
}