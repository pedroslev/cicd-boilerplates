def SaveVersion(Map config) {
    environmentDashboard(addColumns: true, 
        buildJob: "", 
        buildNumber: "${config.version}", 
        componentName: "${config.appName}", 
        data: [], 
        nameOfEnv: "${config.environment}", 
        packageName: '') {
        // some block
    }
}