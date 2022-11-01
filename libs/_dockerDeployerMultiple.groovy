#!/usr/bin/groovy

def call(Map pipelineParams) {
/*----------------------------------------------------------------------------*/
/*                      pipeline for docker                                   */
/*----------------------------------------------------------------------------*/
    pipeline {
        
        agent any

        /*
        Parametes:
        - appName. Docker image Name to deploy
        - versionDocker. Docker version from nexus to deploy in server
        - dockerTag. Tag to deploy
        - serverIP. Server to deploy app. ATENTION: if the port to ssh isn't 22, you must added "-p XX" to parameter
        - appPortExport. Port to publish app.
        - appInternalPort. Port to internal comunication between reverse proxy and app.
        - appPath. Path to save all files about the app (like .env, docker-compose, logs, etc)
        - appReplicasNumber. Application scale number.
        - configurationFile. URL to download the configuration file
        */

        stages {     
            stage("Prepare") {
                steps {
                    script {
                        // create appPathFolder if not exists
                        ssh_.CreateAppPathFolder(server: "${serverIP}",
                                        appPath: "${appPath}/")

                        // replace appName - with _ (mit-api-core -> mit_api_core)
                        params.each { key, value ->
                            if(key.startsWith("version_")){
                                app_version = dockerVersion_.GetVersionFromNexus("${value}") 
                                app_key = key.replaceAll( '-', '_' ) 
                                env."$app_key" = "${app_version}${dockerTag}"
                            }
                        }

                        // for next steps
                        env.appName=""
                    }
                }
            }

            stage('Replace files') {
                steps {
                    script {
                        // download configuration file
                        file_.DownloadConfigurationFile()

                        // load .env file as env vars in pipeline
                        // file_.LoadConfigurationFile()
                        
                        // replace docker-compose folder with envs vars (for example, added appName appPortExport)
                        folder_.ReplaceEnvVars(".docker-compose/")
                    }
                }
            }           
            stage('Docker-compose scp') {
                steps {
                    script {
                        // send .env file
                        ssh_.FileSCP(server: "${serverIP}",
                                        file: ".docker-compose/${appName}.env",
                                        path: "${appPath}/")

                        // send .docker-compose folder
                        ssh_.FolderSCP(server: "${serverIP}",
                                        folder: ".docker-compose/",
                                        path: "${appPath}/")
                    }
                }
            }
            stage('Docker-compose Pull') {
                steps {
                    script {
                        ssh_.DockerComposePull(server: "${serverIP}",
                                        appPath: "${appPath}")
                    }
                }
            }
            stage('Docker-compose down') {
                steps {
                    script {
                        ssh_.DockerComposeDown(server: "${serverIP}",
                                        appPath: "${appPath}")
                    }
                }
            }
            stage('Docker-compose up') {
                steps {
                    script {
                        //create docker network
                        ssh_.DockerNetworkCreate(server: "${serverIP}")

                        //download loki plugin 
                        ssh_.DockerPluginLokiDownload(server: "${serverIP}")

                        ssh_.DockerComposeUp(server: "${serverIP}",
                                        appPath: "${appPath}")
                    }
                }
            }   
        }

        post {
            success {
                script {
                    String environmentToDeploy = "${JOB_NAME}".tokenize('_').last()

                    // Publish in dashboard
                    params.each { key, value ->
                        if(key.startsWith("version_")){
                            app_key = value.tokenize("/")[5]
                            app_version = dockerVersion_.GetVersionFromNexus("${value}") 

                            dashboard_.SaveVersion(appName: "${app_key}",
                                version: "${app_version}",
                                environment: "${environmentToDeploy}")
                        }
                    }
                   
                } 
            }
        }
    }
    
}