def GetVersionFromNexus(String version){
    subversion = version.split("/").last()
    version = subversion.split("-")
    return "${version[0]}-${version[1]}".toString()
}