def Build(Map config) {
    // Remove dangling images
    sh "(docker images -q -f dangling=true | xargs docker rmi -f) || true"
    
    echo "Creating image..."
    sh "docker build -f ${config.file} ${config.buildArgs}  -t ${config.dockerImage} ."
}

def Tag(Map config) {
    echo "Adding tag..."
    sh "docker tag ${config.oldTag} ${config.newTag}"
}

def Pull(Map config) {
    echo "Pushing image..."
    sh "docker pull ${config.dockerImage}"
}

def Push(Map config) {
    echo "Pushing image..."
    sh "docker push ${config.dockerImage}"
}
