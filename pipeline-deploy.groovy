def services = params.service.split(',').toList()
echo "Services array: ${services}"

def dockerLogin() {
    echo "Logging into Docker Registry..."
    sh """
        echo '${DOCKER_REGISTRY_PASSWORD}' | docker login -u '${DOCKER_REGISTRY_USER}' --password-stdin ${DOCKER_REGISTRY_URL}
    """
}

def pullDockerImage(service) {
    echo "Pulling Docker image for service ${service}..."
    sh """docker pull ${DOCKER_REGISTRY_URL}/${service}:latest"""
}

def isServiceRunning(service) {
    def result = sh(script: """docker ps -a --filter "name=${service}" --format "{{.Names}}" """, returnStdout: true).trim()
    return result != ""
}

def serviceDown(service) {
    if (isServiceRunning(service)) {
        echo "Stopping service ${service}..."
        sh """docker-compose -f docker/docker-compose.${service}.yml down"""
    } else {
        echo "Service ${service} is not running, skipping down..."
    }
}

def serviceDeploy(service) {
    echo "Deploying service ${service}..."
    sh """docker-compose -f docker/docker-compose.${service}.yml up -d"""
}

def createDockerNetwork() {
    echo "Checking if Docker network 'online_boutique_network' exists..."
    def networkExists = sh(script: "docker network ls --filter name=online_boutique_network --format '{{.Name}}'", returnStdout: true).trim()
    if (!networkExists) {
        echo "Creating Docker network 'online_boutique_network'..."
        sh "docker network create online_boutique_network"
    } else {
        echo "Docker network 'online_boutique_network' already exists."
    }
}

def sanitizeStageName(name) {
    return name.replaceAll('[^a-zA-Z0-9]', '_')
}

node("build-server") {
    stage("Clone Repository") {
        echo "Cloning GitHub repository..."
        sh """
            rm -rf microservice_online_boutique
            git clone -b main https://github.com/DevopsLPT/microservice_online_boutique.git microservice_online_boutique
        """
        dir('microservice_online_boutique') {
            echo "Repository cloned successfully. Files:"
            sh "ls -l"
        }
    }

    stage("Setup Environment") {
        dockerLogin()
        createDockerNetwork()
    }

    services.each { service ->
        stage(sanitizeStageName(service)) {
            dir('microservice_online_boutique') {
                echo "Running on server: ${env.NODE_NAME}"
                sh "hostname && pwd && ls -l && whoami"

                pullDockerImage(service)
                serviceDown(service)
                serviceDeploy(service)
            }
        }
    }
}
