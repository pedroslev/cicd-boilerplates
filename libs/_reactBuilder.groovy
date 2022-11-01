#!/usr/bin/groovy

def call(Map pipelineParams) {
/*----------------------------------------------------------------------------*/
/*                      pipeline for react                                   */
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
                        def packageJson = readJSON file: 'package.json'

                        // appVersion (x.y.z)
                        env.appVersion = "${packageJson.version}"
                        
                        // appVersionBuild (x.y.z-build_id)
                        env.appVersionBuild = "${packageJson.version}-${BUILD_ID}"
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
                        docker_.Push(dockerImage: "${env.NEXUS_URL}/${appName}:${appVersionBuild}${dockerTag}")
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