def Build(String args="build") {
    echo "Building..."
    sh "./gradlew clean ${args}"
}

def Publish(String args="publish") {
    echo "Publishing..."
    sh "./gradlew ${args}"
}
