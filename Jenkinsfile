pipeline {
    agent any
    environment {
        DOCKER_IMAGE_NAME = "thuanlp/online_boutique_recommendationservice"
        DOCKER_IMAGE_TAG = "latest"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ."
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