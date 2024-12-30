pipeline {
    agent any

    environment {
        AWS_REGION = 'us-east-2' // Specify your AWS region
        IMAGE_NAME = 'wallet-service'
        ECR_REGISTRY = 'public.ecr.aws/z1z0w2y6' // Replace with your ECR public registry
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

        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    // Authenticate to ECR public registry
                    sh """
                    aws ecr-public get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                    """

                    // Build and tag the Docker image
                    sh """
                    docker build -t ${IMAGE_NAME}:latest .
                    docker tag ${IMAGE_NAME}:latest ${ECR_REGISTRY}/${IMAGE_NAME}:latest
                    """
                }
            }
        }

        stage('Push to ECR') {
            steps {
                script {
                    // Push the image to ECR public registry
                    sh """
                    docker push ${ECR_REGISTRY}/${IMAGE_NAME}:latest
                    """
                }
            }
        }
    }

    post {
        always {
            cleanWs() // Clean workspace
            // Optional: Remove Docker images to free up space
            sh """
            docker rmi ${ECR_REGISTRY}/${IMAGE_NAME}:latest || true
            docker rmi ${IMAGE_NAME}:latest || true
            """
        }
    }
}
