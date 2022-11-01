def GetFilesInFolder(String folder) {
    // get files
    files = findFiles(glob: "${folder}/*")
    return files
}

def ReplaceEnvVars(String folder) {
    // get files
    files = GetFilesInFolder(folder)

    // replace files
    files.each { file ->
        // replace env vars in file
        file_.ReplaceEnvVars("${file.path}")
    }
}