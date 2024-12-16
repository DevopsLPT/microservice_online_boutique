def services = params.service.split(',')
echo "Services array: ${services}"

def isServiceRunning(service) {
    def result = sh(script: """docker ps --filter "name=${service}" --format "{{.Names}}" """, returnStdout: true).trim()
    return result ? true : false
}

def serviceDown(service) {
    if (isServiceRunning(service)) {
        echo "Stopping service ${service}..."
        sh """ docker-compose -f docker/docker-compose.${service}.yml down """
    } else {
        echo "Service ${service} is not running, skipping down..."
    }
}

def serviceDeploy(service) {
    echo "Deploying service ${service}..."
    sh """ docker-compose -f docker/docker-compose.${service}.yml up -d """
}

node("boutique-deploy"){
    stage("Clone Repository") {
        echo "Cloning GitHub repository..."
        sh """
            rm -rf microservice_online_boutique
            git clone -b main https://github.com/DevopsLPT/microservice_online_boutique.git microservice_online_boutique
        """
        dir('microservice_online_boutique') {
            echo "Repository cloned successfully. Files:"
            sh """ls -l"""
        }
    }
    
    services.each { service ->
        stage( "$service") {
            dir('microservice_online_boutique') {
                echo "Running on server: ${env.NODE_NAME}"
                sh """hostname && pwd && ls -l && whoami"""
            
                serviceDown(service)
                serviceDeploy(service)
            }
        }
    }
}