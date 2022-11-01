def Tag(Map config){
    try {
        withCredentials([
            usernamePassword(credentialsId: 'jenkins_ldap', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')
        ]) {
            // We need add this configuration to push git tag
            sh 'git config --local credential.helper "!p() { echo username=\\$GIT_USERNAME; echo password=\\$GIT_PASSWORD; }; p"'


            sh "git fetch --all"
            
            sh "git tag -l"

            // Create git tag
            sh "git tag v${config.tag}"

            sh "git push origin v${config.tag}"
        }
    } catch (Exception e) {
        // delete git tag
        sh "git tag -d v${config.tag}"

        echo 'Exception occurred: ' + e.toString()
        sh 'Handle the exception!'
    } finally {
        // We need remove the configuration to push git tag
        sh "git config --local --unset credential.helper"     
    }
}

def Commit(Map config){
    try {
        withCredentials([
            usernamePassword(credentialsId: 'jenkins_ldap', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD')
        ]) {
            // We need add this configuration to push git tag
            sh 'git config --local credential.helper "!p() { echo username=\\$GIT_USERNAME; echo password=\\$GIT_PASSWORD; }; p"'
            
            sh "git push --set-upstream origin ${config.branch}"

            sh "git add ."

            sh "git commit -m 'deployer'"

            sh "git push"
        }
    } catch (Exception e) {
        echo 'Exception occurred: ' + e.toString()
        sh 'Handle the exception!'
    } finally {
        // We need remove the configuration to push git tag
        sh "git config --local --unset credential.helper"     
    }
}