package medical.model

import base.Entity

/**
 * Metadata of a scanned document
 * filename+location
 * import timestamp
 * categories
 * comment
 * invalidation marker
 * content hash
 */
class Document extends Entity {

    String path
    String fileName
    Date imported = new Date()
    def categories = '-'
    String caseNumber
    def hash
    boolean deleted

    @Override
    String getId() {
        createID(caseNumber, fileName)
    }

    static String createID(String caseNumber, String filename) {
        return "$caseNumber::$filename"
    }
}
