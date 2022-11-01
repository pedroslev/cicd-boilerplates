#!/usr/bin/groovy

def call(Map pipelineParams) {
/*----------------------------------------------------------------------------*/
/*                      pipeline for gradle                                   */
/*----------------------------------------------------------------------------*/
    pipeline {

        agent any

        /*
        Parametes:
        - appName. Docker image Name and file name in app/build/libs/${appName}-${appVersion}.jar /opt/app.jar)
        - dockerTag. Tag to added after version and build. It is only to docker image
        - UrlWebhookTeams. URL to connect with Teams Connector
        - environmentToDeploy. Only to show in Teams with notification
        - programmingLanguage. Specific to get properties like version.
        - pushDockerImageToNexus. Selected if you want to push image to nexus.
        */
        
        stages { 
            stage("Prepare") {
                steps {
                    script {         
                        pipeline_.GetProperties("${programmingLanguage}")               
                    }
                    // print vars
                    sh ''' echo "
                    ----- VARS -----
                    appName: ${appName}
                    appVersion: ${appVersion}
                    appVersionBuild: ${appVersionBuild}
                    ----------------"
                    '''
                }
            }
            stage('Docker Build') {
                steps {
                    script{
                        docker_.Build(file: ".docker/Dockerfile",
                                    buildArgs: "",
                                    dockerImage: "${env.NEXUS_URL}/${appName}:${appVersionBuild}${dockerTag}")
                    }
                }
            }
            stage('Docker Push') {
                steps {
                    script{
                        if(pushDockerImageToNexus.toBoolean()){
                            docker_.Push(dockerImage: "${env.NEXUS_URL}/${appName}:${appVersionBuild}${dockerTag}")
                        }
                    }
                }
            }  
        }

        post {
            success {
                script {
                    teams_.NewVersion(urlWebhookTeams: "${UrlWebhookTeams}",
                        appName: "${appName}",
                        appVersion: "${appVersionBuild}" ,
                        environment: "${environmentToDeploy}")

                    dashboard_.SaveVersion(appName: "${appName}",
                        version: "${appVersionBuild}",
                        environment: "${environmentToDeploy}")
                } 
            }
        }
    }


}