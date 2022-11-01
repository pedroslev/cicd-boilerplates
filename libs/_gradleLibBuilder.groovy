#!/usr/bin/groovy

def call(Map pipelineParams) {
/*----------------------------------------------------------------------------*/
/*                      pipeline for lib gradle                               */
/*----------------------------------------------------------------------------*/
    pipeline {

        agent any

        /*
        Parametes:
        - appName. Docker image Name and file name in app/build/libs/${appName}-${appVersion}.jar /opt/app.jar)
        - dockerTag. Tag to added after version and build. It is only to docker image
        - filenameToSaveVersion. Filename when jenkins write the new version to deploy in server). Local Path:  ~/jenkins-job-versions/ 
        - UrlWebhookTeams. URL to connect with Teams Connector
        - environmentToDeploy. Only to show in Teams with notification
        */
        
        stages { 
            stage("Prepare") {
                steps {
                    script {
                        // get properties
                        def props = file_.GetProperties("gradle.properties")

                        // appVersion (x.y.z)
                        env.appVersion = "${props.version}"
                        
                        // appVersionBuild (x.y.z-build_id)
                        env.appVersionBuild = "${props.version}-${BUILD_ID}"
                    
                        // replace gradle.properties with envs vars (for example, added build_id number)
                        file_.ReplaceEnvVars("gradle.properties")
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
                                    dockerImage: "${appName}:${appVersionBuild}${dockerTag}")
                    }
                }
            }
            stage('Save Version') {
                steps {
                    script{
                        file_.SaveInFile(echo: "${appVersionBuild}",
                                    file: "${filenameToSaveVersion}")
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

                    String componentName = "${filenameToSaveVersion}".split("_")[0]
                    dashboard_.SaveVersion(appName: "${componentName}",
                        version: "${appVersionBuild}",
                        environment: "${environmentToDeploy}")
                } 
            }
        }
    }


}