pipeline {
    agent { label 'build-server' } 
    environment {
        PROJECT = "shippingservice"
        DOCKER_IMAGE_NAME = "harbor.lptdevops.website/online_boutique/shippingservice"
        DOCKER_IMAGE_TAG = "latest"
        REPORT_TRIVY_NAME = "online_boutique_shippingservice_trivy_report"
        REGISTRY_URL = "harbor.lptdevops.website"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Sonarqube_Static_Code_Analysis') {
            steps {
                withCredentials([
                    string(credentialsId: 'SONAR_HOST', variable: 'SONAR_HOST'),
                    string(credentialsId: 'SONAR_TOKEN_SHIPPING', variable: 'SONAR_TOKEN'),
                    string(credentialsId: 'SONAR_PROJECTKEY_SHIPPING', variable: 'SONAR_PROJECTKEY')
                ]) {
                    script {
                        sh """
                            docker run --rm \
                                -v ${WORKSPACE}:/usr/src \
                                sonarsource/sonar-scanner-cli:latest \
                                sonar-scanner \
                                -Dsonar.host.url=${SONAR_HOST} \
                                -Dsonar.token=${SONAR_TOKEN} \
                                -Dsonar.projectKey=${SONAR_PROJECTKEY}
                        """
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ."
                }
            }
        }

        stage('Trivy_Image_Scan') {
            steps {
                script {
                    sh """
                        docker run --rm -v ${WORKSPACE}:/${PROJECT} -v \
                            /var/run/docker.sock:/var/run/docker.sock \
                            aquasec/trivy image --download-db-only
        
                        docker run --rm -v ${WORKSPACE}:/${PROJECT} -v /var/run/docker.sock:/var/run/docker.sock \
                            aquasec/trivy image --format template --template "@contrib/html.tpl" \
                            --output ${PROJECT}/${REPORT_TRIVY_NAME}.html ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}
                    """
                }
            }
        }

        stage('Login to Docker Registry') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'harbor', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            echo $DOCKER_PASS | docker login ${REGISTRY_URL} -u $DOCKER_USER --password-stdin
                        """
                    }
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    sh "docker push ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
                }
            }
        }


    }
    post {
        always {
            sh "docker logout ${REGISTRY_URL}"
        }
        success {
            echo "Docker image ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} pushed successfully!"
        }
        failure {
            echo "Build or push failed. Please check the logs."
        }
    }
}