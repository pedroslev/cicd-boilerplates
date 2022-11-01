#!/usr/bin/groovy

def call(Map pipelineParams) {
    pipeline {
        
        agent any

        /*
        Parametes:
        - dockerTag. Tag to deploy
        */

        stages {     
            stage("Prepare") {
                steps {
                    script {
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
                        // replace docker-compose folder with envs vars (for example, added appName appPortExport)
                        folder_.ReplaceEnvVars(".docker-compose/")
                    }
                }
            }    

            stage('Push files') {
                steps {
                    script {
                        dir('bitbucketRepo') {
                            git branch: "${bitbucketBranch}",
                                credentialsId: "jenkins_bitbucket", 
                                url: "${bitbucketRepo}"
                        }                         

                        sh "cp -r .docker-compose/. bitbucketRepo/"

                        dir('bitbucketRepo') {
                            git_.Commit(branch: "${bitbucketBranch}")
                        }                         
                    }
                }
            }      
        }
    }    
}