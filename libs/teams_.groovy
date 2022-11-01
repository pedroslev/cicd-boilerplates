def NewVersion(Map config){
    office365ConnectorSend webhookUrl: "${config.urlWebhookTeams}",
        message: "Nueva version disponible", 
        color: '#65D51C',
        factDefinitions: [
            [name: "aplicación", template: "${config.appName}"],
            [name: "version", template: "${config.appVersion}"],
            [name: "ambiente", template: "${config.environment}"]
        ]       
}