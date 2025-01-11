pipeline {
    agent any

    environment {
        AWS_REGION      = 'us-east-1'
        IMAGE_NAME      = 'wallet-service'
        ECR_REGISTRY    = 'public.ecr.aws/z1z0w2y6'
        DOCKER_BUILD_NUMBER = "${BUILD_NUMBER}"
        EKS_CLUSTER_NAME = 'main-cluster'
        NAMESPACE = 'fintech'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    try {
                        sh 'mvn clean package -DskipTests'
                    } catch (Exception e) {
                        error "Maven build failed: ${e.message}"
                    }
                }
            }
        }

        stage('Build & Push Docker Image') {
            steps {
                script {
                    withCredentials([[
                        $class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: 'aws-credentials',
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                    ]]) {
                        try {
                            sh """
                                aws ecr-public get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                            """

                            sh """
                                docker build -t ${IMAGE_NAME}:${DOCKER_BUILD_NUMBER} . --no-cache
                                docker tag ${IMAGE_NAME}:${DOCKER_BUILD_NUMBER} ${ECR_REGISTRY}/${IMAGE_NAME}:${DOCKER_BUILD_NUMBER}
                                docker tag ${IMAGE_NAME}:${DOCKER_BUILD_NUMBER} ${ECR_REGISTRY}/${IMAGE_NAME}:latest
                            """

                            sh """
                                docker push ${ECR_REGISTRY}/${IMAGE_NAME}:${DOCKER_BUILD_NUMBER}
                                docker push ${ECR_REGISTRY}/${IMAGE_NAME}:latest
                            """
                        } catch (Exception e) {
                            error "Docker build/push failed: ${e.message}"
                        }
                    }
                }
            }
        }

        stage('Deploy to EKS') {
            steps {
                script {
                    withCredentials([[
                        $class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: 'aws-credentials',
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                    ]]) {
                        try {
                            // Configure kubectl
                            sh """
                                aws eks update-kubeconfig --region ${AWS_REGION} --name ${EKS_CLUSTER_NAME}
                            """

                            // Create namespace if doesn't exist
                            sh """
                                kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
                            """

                            // Update deployment file with new image
                            sh """
                                sed -i 's|${ECR_REGISTRY}/${IMAGE_NAME}:[0-9]*|${ECR_REGISTRY}/${IMAGE_NAME}:${DOCKER_BUILD_NUMBER}|g' k8s/deployment.yaml
                            """

                            // Apply K8s manifests
                            sh """
                                kubectl apply -f k8s/configmap.yaml -n ${NAMESPACE}
                                kubectl apply -f k8s/secrets.yaml -n ${NAMESPACE}
                                kubectl apply -f k8s/deployment.yaml -n ${NAMESPACE}
                                kubectl apply -f k8s/service.yaml -n ${NAMESPACE}
                            """

                            // Just check pod status
                            sh """
                                echo "Checking pod status:"
                                kubectl get pods -n ${NAMESPACE} -l app=wallet-service
                            """

                        } catch (Exception e) {
                            error "Deployment failed: ${e.message}"
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline succeeded! Application deployed successfully.'
        }
        failure {
            echo 'Pipeline failed! Check the logs for details.'
        }
        always {
            // Cleanup
            sh """
                docker rmi ${ECR_REGISTRY}/${IMAGE_NAME}:${DOCKER_BUILD_NUMBER} || true
                docker rmi ${ECR_REGISTRY}/${IMAGE_NAME}:latest || true
                docker rmi ${IMAGE_NAME}:${DOCKER_BUILD_NUMBER} || true
            """
            cleanWs()
        }
    }
}