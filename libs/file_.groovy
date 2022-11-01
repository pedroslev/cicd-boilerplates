def GetProperties(String file) {
    String content = readFile("${file}")
    Properties properties = new Properties()
    properties.load(new StringReader(content))
    return properties
}

def ReplaceEnvVars(String file) {
    sh "envsubst \"\$(env | cut -d= -f1 | sed -e 's/^/\$/')\" < ${file} > ${file}.temp"
    sh "mv ${file}.temp ${file}"
}

def SaveInFile(Map config){
    echo "Saving version in local storage..."
    sh "echo '${config.echo}' > ~/jenkins-job-versions/${config.file}"
}

def ReadFile(String file) {
    return sh (
        script: "cat ~/jenkins-job-versions/${file}",
        returnStdout: true
    ).trim()
}

def DownloadBitbucketFile(Map config){
    withCredentials([usernamePassword(credentialsId: 'jenkins_ldap', usernameVariable: "USERNAME", passwordVariable: "PASSWORD")]) {
        sh "curl -u '${USERNAME}:${PASSWORD}' ${config.url} > ${config.file}"
    }
}

def DownloadConfigurationFile(String configurationFile="${JOB_NAME}/${appName}.env"){
    dir('cfg') {
        git credentialsId: 'jenkins_ldap',
            url: 'https://bitbucket.org/mitrol/cicd-configuration-files.git'
    }    
    sh "cp cfg/${configurationFile} .docker-compose/"
    sh "rm -rf cfg"
}

def LoadConfigurationFile(String configurationFile=".docker-compose/${appName}.env"){
    // read .env file
    props = readProperties file: "${configurationFile}"

    // load env vars
    props.each {key, value -> env[key] = value }
}