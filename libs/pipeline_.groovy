def GetProperties(String programmingLanguage){
    Map props
    switch("${programmingLanguage}") {
        case "gradle":
            // get properties
            props = readProperties file: "gradle.properties"
            // replace gradle.properties with envs vars (for example, added build_id number)
            file_.ReplaceEnvVars("gradle.properties")
            break
         case "maven":
            // replace pom.xml with envs vars (for example, added build_id number)
            file_.ReplaceEnvVars("pom.xml")            
            // get properties
            pom = readMavenPom file: "pom.xml"

            // net.mitrol:WebPad:jar:3.11.5-3
            versionPOM = pom.version.tokenize(":").last()
            echo "versionPOM: ${versionPOM}"
            
            // 3.11.5-3
            versionPOM = versionPOM.tokenize("-").first()
            echo "versionPOM: ${versionPOM}"

            props = [ "version": "${versionPOM}" ]
            break

        case "proxy":
            env.TAG="${dockerTag}"

            // replace package.json with envs vars (for example, added build_id number)
            file_.ReplaceEnvVars("package.json")    
            
            // get properties
            packageJson = readJSON file: 'package.json'

            // 1.0.1-44
            versionPackage = packageJson.version.tokenize("-").first()
            echo "versionPackage: ${versionPackage}"

            props = [ "version": "${versionPackage}" ]
            break

        case "react":
            env.TAG="${dockerTag}"

            // replace package.json with envs vars (for example, added build_id number)
            file_.ReplaceEnvVars("package.json")    
            
            // get properties
            packageJson = readJSON file: 'package.json'

            // 1.0.1-44
            versionPackage = packageJson.version.tokenize("-").first()
            echo "versionPackage: ${versionPackage}"

            props = [ "version": "${versionPackage}" ]
            break
        default:
            break
    }
    // appVersion (x.y.z)
    env.appVersion = "${props.version}"        
    // appVersionBuild (x.y.z-build_id)
    env.appVersionBuild = "${props.version}-${BUILD_ID}"
}