pipeline {
    agent any

    environment {
        AWS_ACCOUNT_ID = credentials('AWS_ACCOUNT_ID')
        AWS_REGION = 'us-east-1'  // Change to your AWS region
        ECR_REPO = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        IMAGE_NAME = 'wallet-service'
        IMAGE_TAG = "${BUILD_NUMBER}"
        EKS_CLUSTER_NAME = 'your-eks-cluster'  // Change to your cluster name
        NAMESPACE = 'wallet-service'
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

        stage('Unit Tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Build and Push Docker Image to ECR') {
            steps {
                script {
                    // Authenticate with ECR
                    sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}"

                    // Build Docker image
                    docker.build("${ECR_REPO}/${IMAGE_NAME}:${IMAGE_TAG}")

                    // Push to ECR
                    sh "docker push ${ECR_REPO}/${IMAGE_NAME}:${IMAGE_TAG}"
                    sh "docker push ${ECR_REPO}/${IMAGE_NAME}:latest"
                }
            }
        }

        stage('Deploy to EKS') {
            steps {
                script {
                    // Configure kubectl
                    sh "aws eks update-kubeconfig --region ${AWS_REGION} --name ${EKS_CLUSTER_NAME}"

                    // Create namespace if it doesn't exist
                    sh "kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -"

                    // Deploy the k8s resources
                    withKubeConfig([credentialsId: 'eks-credentials']) {
                        // Apply Kubernetes manifests
                        sh """
                            sed -i 's|IMAGE_URL_PLACEHOLDER|${ECR_REPO}/${IMAGE_NAME}:${IMAGE_TAG}|g' k8s/deployment.yaml
                            kubectl apply -f k8s/configmap.yaml -n ${NAMESPACE}
                            kubectl apply -f k8s/deployment.yaml -n ${NAMESPACE}
                            kubectl apply -f k8s/service.yaml -n ${NAMESPACE}
                        """

                        // Wait for deployment to complete
                        sh "kubectl rollout status deployment/${IMAGE_NAME} -n ${NAMESPACE}"
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
            slackSend(color: 'good', message: "Success: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }
        failure {
            echo 'Pipeline failed!'
            slackSend(color: 'danger', message: "Failed: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }
    }
}
