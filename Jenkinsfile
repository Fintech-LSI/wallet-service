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
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build and Push Docker Image') {
            steps {
                script {
                    withAWS(credentials: 'aws-credentials', region: AWS_REGION) {
                        sh """
                            aws ecr-public get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                            docker build -t ${IMAGE_NAME}:${DOCKER_BUILD_NUMBER} .
                            docker tag ${IMAGE_NAME}:${DOCKER_BUILD_NUMBER} ${ECR_REGISTRY}/${IMAGE_NAME}:${DOCKER_BUILD_NUMBER}
                            docker push ${ECR_REGISTRY}/${IMAGE_NAME}:${DOCKER_BUILD_NUMBER}
                        """
                    }
                }
            }
        }

        stage('Deploy to EKS') {
            steps {
                script {
                    withAWS(credentials: 'aws-credentials', region: AWS_REGION) {
                        // Update kubeconfig
                        sh "aws eks update-kubeconfig --name ${EKS_CLUSTER_NAME} --region ${AWS_REGION}"

                        // Create namespace if not exists
                        sh "kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f - --validate=false"

                        // Apply K8s configs
                        sh """
                            sed -e 's|\${ECR_REGISTRY}|${ECR_REGISTRY}|g' \
                                -e 's|\${IMAGE_NAME}|${IMAGE_NAME}|g' \
                                -e 's|\${DOCKER_BUILD_NUMBER}|${DOCKER_BUILD_NUMBER}|g' \
                                k8s/deployment.yaml > deployment-updated.yaml

                            kubectl apply -f k8s/configmap.yaml -n ${NAMESPACE} --validate=false
                            kubectl apply -f k8s/service.yaml -n ${NAMESPACE} --validate=false
                            kubectl apply -f deployment-updated.yaml -n ${NAMESPACE} --validate=false

                            kubectl rollout status deployment/wallet-service -n ${NAMESPACE}
                            kubectl get pods -n ${NAMESPACE} -l app=wallet-service
                        """
                    }
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
                rm -f deployment-updated.yaml
            """
        }
    }
}
