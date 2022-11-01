def DockerPull(Map config){
    sh "ssh jenkins@${config.server} docker pull ${config.dockerImage}"
}

def FileSCP(Map config){
    try {
        // If the server has a specific port (ex: 192.168.1.2 -p 5423)
        serverIp=config.server.split(" ")[0]
        port=config.server.split(" ")[2]
        sh "scp -P ${port} ${config.file} jenkins@${serverIp}:${config.path}"
    } catch (Exception e) {
        sh "scp ${config.file} jenkins@${serverIp}:${config.path}"  
    } 
}

def FolderSCP(Map config){
    try {
        // If the server has a specific port (ex: 192.168.1.2 -p 5423)
        serverIp=config.server.split(" ")[0]
        port=config.server.split(" ")[2]
        sh "scp -r -P ${port} ${config.folder}* jenkins@${serverIp}:${config.path}"
    } catch (Exception e) {
        sh "scp -r ${config.folder}* jenkins@${serverIp}:${config.path}"  
    } 
}

def DockerComposeDown(Map config){
    sh "ssh jenkins@${config.server} 'cd ${config.appPath}/ && docker-compose down'"
}

def DockerComposePull(Map config){
    sh "ssh jenkins@${config.server} 'cd ${config.appPath}/ && docker-compose pull'"
}

def DockerComposeUp(Map config){
    if(config.scale != null && config.appName != null){
        sh "ssh jenkins@${config.server} 'cd ${config.appPath}/ && docker-compose up -d --remove-orphans --scale ${config.appName}=${config.scale}'"
    }else{
        sh "ssh jenkins@${config.server} 'cd ${config.appPath}/ && docker-compose up -d --remove-orphans'"
    }
}

def DockerPluginLokiDownload(Map config){
    try {
        sh "ssh jenkins@${config.server} 'docker plugin install grafana/loki-docker-driver:latest --alias loki --grant-all-permissions'"
        echo "docker plugin loki created."
    } catch (Exception e) {
        echo "docker plugin loki exists."
    }
}

def DockerNetworkCreate(Map config){
    try {
        sh "ssh jenkins@${config.server} 'docker network create mitrol-network'"
        echo "docker network created."
    } catch (Exception e) {
        echo "docker network exists."
    }
}

def CreateAppPathFolder(Map config){
    sh "ssh jenkins@${config.server} mkdir -p ${config.appPath}"
}
