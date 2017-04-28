package medical.external

/**
 * represents a scanner
 * the scanner is capable of resolving barcodes to filenames
 */
class DocumentScanner {

    private int scanNumber = 1

    /**
     * the scanner must be attached to a filestore
     */
    FileStore attachedFileStore

    /**
     * the scanner must be configured to write files to this directory
     */
    String importLocation

    /**
     * scan a document with a case label (barcode)
     * @param content    represents the content of the document
     * @param caseNumber  the case number that the barcode represents
     */
    def scan(String content, String caseNumber) {
        def filename = createFileName(caseNumber)
        attachedFileStore.writeFile(importLocation, filename, content)
    }

    /**
     * the scanner must create a unique filename from a given casenumber
     * @param caseNumber
     */
    def createFileName(String caseNumber) {
        Date now = new Date()
        "${caseNumber}-Scan-${scanNumber++}-${now.toString()}.pdf"
    }
}
