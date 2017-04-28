package medical.external

/**
 * abstraction of a filestore
 *
 * a filestore is a physical storage location
 * e.g. a mounted filesystem or NAS that can be identified by a file URL
 */
class FileStore {

    private files = [:]

    /**
     * descend a path and return the directory at the end of the path
     * @param path
     * @return
     */
    def descend(String path) {
        def pathElements = path.tokenize('/')
        def current = files
        pathElements.each {
            current = current[it]
        }
        current
    }

    /**
     * list a directory
     * @param path
     * @return the files of the directory as a Map: filename -> content
     */
    def listDirectory(String path) {
        def directory = descend(path)
        return directory ?: [:]
    }

    def createDirectory(String path, String directory) {
        def dir = descend(path)
        if ( !dir.get(directory) )
            dir.put(directory, [:])
        return dir
    }

    def readFile(String path, String filename) {
        def directory = descend(path)
        directory?.get(filename)
    }

    def readFile(String location) {
        def pathIndex = location.lastIndexOf('/')
        def path = location.substring(0, pathIndex)
        def filename = location.substring(pathIndex+1)
        def directory = descend(path)
        return directory?.get(filename)
    }

    def writeFile(String path, String filename, content) {
        def directory = descend(path)
        directory.put(filename, content)
    }

    def moveFile(String oldPath, String newPath, String filename) {
        def directory = descend(oldPath)
        def content = directory[filename]
        directory.remove(filename)
        writeFile(newPath, filename, content)
    }

    def deleteFile(String path, String filename) {
        def directory = descend(path)
        directory.remove(filename)
    }
}

