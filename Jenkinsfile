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

        stage('Login to EKS') {
            steps {
                script {
                    withCredentials([[
                        $class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: 'aws-credentials',
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                    ]]) {
                        sh """
                            aws eks get-token --cluster-name ${EKS_CLUSTER_NAME} --region ${AWS_REGION}
                            aws eks update-kubeconfig --region ${AWS_REGION} --name ${EKS_CLUSTER_NAME}
                            kubectl config use-context arn:aws:eks:${AWS_REGION}:640168414375:cluster/${EKS_CLUSTER_NAME}
                        """
                    }
                }
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
                        // Create namespace if it doesn't exist
                        sh "kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -"

                        // Apply Kubernetes configurations
                        sh """
                            # Update deployment image
                            sed -e 's|\${ECR_REGISTRY}|${ECR_REGISTRY}|g' \
                                -e 's|\${IMAGE_NAME}|${IMAGE_NAME}|g' \
                                -e 's|\${DOCKER_BUILD_NUMBER}|${DOCKER_BUILD_NUMBER}|g' \
                                k8s/deployment.yaml > deployment-updated.yaml

                            kubectl apply -f k8s/configmap.yaml -n ${NAMESPACE}
                            kubectl apply -f k8s/service.yaml -n ${NAMESPACE}
                            kubectl apply -f deployment-updated.yaml -n ${NAMESPACE}

                            # Verify deployment
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