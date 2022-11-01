#!/usr/bin/groovy

def call(Map pipelineParams) {
/*----------------------------------------------------------------------------*/
/*                      pipeline for gradle                                   */
/*----------------------------------------------------------------------------*/
    pipeline {
        
        agent any

        /*
        Parametes:
        - dockerImageName. Docker image Name to promote
        - dockerOldTag. Tag to promote
        - dockerNewTag. New tag for docker image
        - versionDocker. Docker version from nexus to promote
        - UrlWebhookTeams. URL to connect with Teams Connector
        - environmentToDeploy. Only to show in Teams with notification
        - createGitTag. Selected if you want create a git tag.
        */

        stages { 
            stage("Prepare") {
                steps {
                    script {
                        // appVersionBuild (x.y.z-build)
                        env.appVersionBuild = dockerVersion_.GetVersionFromNexus("${versionDocker}")
                    }
                    
                    // print vars
                    sh ''' echo "
                    ----- VARS -----
                    appVersionBuild: ${appVersionBuild}
                    ----------------"
                    '''
                }
            }
            stage('Git tag') {
                steps {
                    script {
                        if(createGitTag.toBoolean()){
                            git_.Tag(tag: "${appVersionBuild}")
                        }
                    }
                }
            }     
            stage('Docker pull') {
                steps {
                    script {
                        docker_.Pull(dockerImage: "${env.NEXUS_URL}/${dockerImageName}:${appVersionBuild}${dockerOldTag}")
                    }
                }
            }
            stage('Docker tag') {
                steps {
                    script{
                        docker_.Tag(oldTag: "${env.NEXUS_URL}/${dockerImageName}:${appVersionBuild}${dockerOldTag}",
                                newTag: "${env.NEXUS_URL}/${dockerImageName}:${appVersionBuild}${dockerNewTag}")
                    }
                }
            }
            stage('Docker push') {
                steps {
                    script{
                        docker_.Push(dockerImage: "${env.NEXUS_URL}/${dockerImageName}:${appVersionBuild}${dockerNewTag}")
                    }
                }
            }  
        }  

        post {
            success {
                script {
                    teams_.NewVersion(urlWebhookTeams: "${UrlWebhookTeams}",
                        appName: "${dockerImageName}",
                        appVersion: "${appVersionBuild}" ,
                        environment: "${environmentToDeploy}")

                    dashboard_.SaveVersion(appName: "${dockerImageName}",
                        version: "${appVersionBuild}",
                        environment: "${environmentToDeploy}")
                } 
            }
        }
    }
}