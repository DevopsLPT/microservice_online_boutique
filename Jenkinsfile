pipeline {
    agent { label 'build-server' } 
    environment {
        DOCKER_IMAGE_NAME = "thuanlp/online_boutique_frontend"
        DOCKER_IMAGE_TAG = "latest"
        REPORT_SNYK_NAME = "online_boutique_fe_snyk_report"
        SNYK_IMAGE_NAME = "snyk_scan_image"
        SNYK_CONTAINER_NAME = "snyk_scan"
        REPORT_TRIVY_NAME = "online_boutique_fe_trivy_report"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // stage('Sonarqube_Static_Code_Analysis') {
        //     steps {
        //         withCredentials([
        //             string(credentialsId: 'SONAR_HOST', variable: 'SONAR_HOST'),
        //             string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN'),
        //             string(credentialsId: 'SONAR_PROJECTKEY', variable: 'SONAR_PROJECTKEY')
        //         ]) {
        //             script {
        //                 sh """
        //                     docker run --rm \
        //                         -v ${env.WORKSPACE}:/usr/src \
        //                         sonarsource/sonar-scanner-cli:latest \
        //                         sonar-scanner \
        //                         -Dsonar.host.url=${SONAR_HOST} \
        //                         -Dsonar.token=${SONAR_TOKEN} \
        //                         -Dsonar.projectKey=${SONAR_PROJECTKEY}
        //                 """
        //             }
        //         }
        //     }
        // }

        stage('Snyk_Composition_Analysis') {
            steps {
                withCredentials([
                    string(credentialsId: 'SNYK_TOKEN', variable: 'SNYK_TOKEN'),

                ]) {
                    script {
                        sh """
                            docker build --rm \
                                --build-arg SNYK_AUTH_TOKEN=${SNYK_TOKEN } \
                                --build-arg OUTPUT_FILENAME=${REPORT_SNYK_NAME} \
                                -t ${SNYK_IMAGE_NAME} -f Dockerfile-snyk .
                            
                            docker run --name ${SNYK_CONTAINER_NAME} ${SNYK_IMAGE_NAME}
                            
                            docker cp ${SNYK_CONTAINER_NAME}:/app/${REPORT_SNYK_NAME}.html ./${REPORT_SNYK_NAME}.html
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
                        docker run --rm -v $PWD:/shoeshop_fe -v \
                            /var/run/docker.sock:/var/run/docker.sock \
                            aquasec/trivy image --download-db-only
        
                        docker run --rm -v $PWD:/shoeshop_fe -v /var/run/docker.sock:/var/run/docker.sock \
                            aquasec/trivy image --format template --template "@contrib/html.tpl" \
                            --output /shoeshop_fe/${REPORT_TRIVY_NAME}.html ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}
                    """
                }
            }
        }

        stage('Login to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
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
            sh "docker logout"
        }
        success {
            echo "Docker image ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} pushed successfully!"
        }
        failure {
            echo "Build or push failed. Please check the logs."
        }
    }
}