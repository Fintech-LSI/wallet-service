pipeline {
    agent any

    environment {
        AWS_REGION      = 'us-east-1'
        IMAGE_NAME      = 'wallet-service'
        ECR_REGISTRY    = 'public.ecr.aws/z1z0w2y6'
        DOCKER_BUILD_NUMBER = "${BUILD_NUMBER}"
        EKS_CLUSTER_NAME = 'main-cluster'
        NAMESPACE = 'fintech'
        AWS_ACCOUNT_ID = '640168414375'
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

        stage('Configure AWS & Kubectl') {
            steps {
                script {
                    withCredentials([[
                        $class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: 'aws-credentials',
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                    ]]) {
                        sh """
                            aws configure set aws_access_key_id ${AWS_ACCESS_KEY_ID}
                            aws configure set aws_secret_access_key ${AWS_SECRET_ACCESS_KEY}
                            aws configure set region ${AWS_REGION}
                            aws configure set output json

                            # Update kubeconfig
                            aws eks update-kubeconfig --region ${AWS_REGION} --name ${EKS_CLUSTER_NAME}

                            # Verify configuration
                            kubectl config current-context
                            kubectl cluster-info
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
                            aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin ${ECR_REGISTRY}
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
                        sh """
                            # Create namespace if it doesn't exist
                            kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

                            # Update deployment image
                            sed -e 's|\${ECR_REGISTRY}|${ECR_REGISTRY}|g' \
                                -e 's|\${IMAGE_NAME}|${IMAGE_NAME}|g' \
                                -e 's|\${DOCKER_BUILD_NUMBER}|${DOCKER_BUILD_NUMBER}|g' \
                                k8s/deployment.yaml > deployment-updated.yaml

                            # Debug output
                            echo "Applying ConfigMap..."
                            cat k8s/configmap.yaml

                            echo "Applying Service..."
                            cat k8s/service.yaml

                            echo "Applying Deployment..."
                            cat deployment-updated.yaml

                            # Apply Kubernetes configurations
                            kubectl apply -f k8s/configmap.yaml -n ${NAMESPACE} --validate=false
                            kubectl apply -f k8s/service.yaml -n ${NAMESPACE} --validate=false
                            kubectl apply -f deployment-updated.yaml -n ${NAMESPACE} --validate=false

                            # Wait for deployment
                            kubectl rollout status deployment/wallet-service -n ${NAMESPACE} --timeout=300s

                            # Debug info
                            echo "Deployment Status:"
                            kubectl get deployments -n ${NAMESPACE}
                            echo "Pods Status:"
                            kubectl get pods -n ${NAMESPACE}
                            echo "Services Status:"
                            kubectl get services -n ${NAMESPACE}
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
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed! Check the logs for details.'
        }
    }
}