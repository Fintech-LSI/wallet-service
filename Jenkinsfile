pipeline {
    agent any

    environment {
        AWS_REGION      = 'us-east-1'
        IMAGE_NAME      = 'wallet-service'
        ECR_REGISTRY    = 'public.ecr.aws/z1z0w2y6'
        DOCKER_BUILD_NUMBER = "${BUILD_NUMBER}"
        EKS_CLUSTER_NAME = 'main-cluster'
        NAMESPACE = 'fintech'
        KUBECONFIG = "${WORKSPACE}/.kube/config"
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
                    withCredentials([[
                        $class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: 'aws-credentials',
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                    ]]) {
                        sh """
                            aws ecr-public get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                            docker build -t ${IMAGE_NAME}:${DOCKER_BUILD_NUMBER} .
                            docker tag ${IMAGE_NAME}:${DOCKER_BUILD_NUMBER} ${ECR_REGISTRY}/${IMAGE_NAME}:${DOCKER_BUILD_NUMBER}
                        """
                    }
                }
            }
        }

        stage('Push to ECR') {
            steps {
                script {
                    withCredentials([[
                        $class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: 'aws-credentials',
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                    ]]) {
                        sh "docker push ${ECR_REGISTRY}/${IMAGE_NAME}:${DOCKER_BUILD_NUMBER}"
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
                        // Create .kube directory if it doesn't exist
                        sh "mkdir -p ${WORKSPACE}/.kube"

                        // Update kubeconfig with explicit path
                        sh """
                            aws eks update-kubeconfig --region ${AWS_REGION} \
                                --name ${EKS_CLUSTER_NAME} \
                                --kubeconfig ${KUBECONFIG}
                        """

                        // Verify cluster access
                        sh """
                            kubectl --kubeconfig=${KUBECONFIG} auth can-i create namespace
                            if [ \$? -ne 0 ]; then
                                echo "Failed to authenticate with EKS cluster"
                                exit 1
                            fi
                        """

                        // Create namespace if it doesn't exist
                        sh """
                            kubectl --kubeconfig=${KUBECONFIG} create namespace ${NAMESPACE} \
                                --dry-run=client -o yaml | \
                                kubectl --kubeconfig=${KUBECONFIG} apply -f -
                        """

                        // Replace variables in deployment manifest
                        sh """
                            sed 's|\${ECR_REGISTRY}|${ECR_REGISTRY}|g; \
                                s|\${IMAGE_NAME}|${IMAGE_NAME}|g; \
                                s|\${DOCKER_BUILD_NUMBER}|${DOCKER_BUILD_NUMBER}|g' \
                                k8s/deployment.yaml > deployment-updated.yaml
                        """

                        // Apply manifests with explicit kubeconfig
                        sh """
                            kubectl --kubeconfig=${KUBECONFIG} apply -f deployment-updated.yaml -n ${NAMESPACE}
                            kubectl --kubeconfig=${KUBECONFIG} apply -f k8s/service.yaml -n ${NAMESPACE}
                        """

                        // Wait for rollout
                        sh """
                            kubectl --kubeconfig=${KUBECONFIG} rollout status deployment/wallet-service \
                                -n ${NAMESPACE} --timeout=300s
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
                rm -f deployment-updated.yaml || true
            """
        }
    }
}