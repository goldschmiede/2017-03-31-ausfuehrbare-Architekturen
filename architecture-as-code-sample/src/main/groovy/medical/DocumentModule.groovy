package medical

import medical.external.FileStore
import base.Repository
import base.UI
import medical.model.Document

/**
 * a document is scanned using a scanner
 *  or moved to a filestore from an associated computer
 *  or created by a module
 * note: scanner can identify a casenumber label and move the document directly to a directory for that casenumber
 *  a document must be registered with a casenumber
 *  a document must be categorized (document type)
 * several versions of a document can exist - the latest version is the relevant one
 * need access control (as for patients)
 *
 * operations:
 *      periodically scan a configurable import location (file store) for new documents
 *      import new documents:
 *          extract case-number from filename
 *          store in case-document storage
 *          calculate content hash
 *          create metadata entry
 *      list documents in a sortable, filterable overview
 *      show number of documents without categories
 *      list documents per case
 *      jump to case-module for selected document
 *      preview a selected document - at least first page
 *      download selected document
 *      mark selected document as invalid with prompting the user
 *          consequence: an invalid document is not displayable or downloadable
 *      store document (API for other modules) for a given case-number, category and content
 *      check routine (either before listing or periodically)
 *          ensure content hash, modification date per entry
 *          ensure invalid document is deleted from document storage - delete if present
 */
class DocumentModule {

    /**
     * the document filestore is organized into:
     *   /<configured-location>/<case-number>/* - documents assigned to a case
     */
    FileStore documentStore

    /**
     * the filestore where the scanned files are located
     */
    FileStore importStore

    /**
     * the module is based upon a repository
     * the repository contains only the metadata, not the real files
     */
    Repository<Document> documents = new Repository<Document>() {
        @Override
        def search(Map<String, String> filter) {
            checkDocuments()
            return super.search(filter)
        }

        // ensure modification date and hash are correct
        def checkDocuments() {
        }
    }

    /**
     * the Document UI is a standard UI with extensions
     * the edit form allows to assign categories from a predefined list of categories
     */
    class DocumentUI extends UI<Document> {

        /**
         * a UI element displaying the number of uncategorized documents
         */
        def nDocs = numberOfUncategorizedDocuments()

        @Override
        def search() {
            nDocs = numberOfUncategorizedDocuments()
            return super.search()
        }

        @Override
        def save() {
            def result = super.save()
            nDocs = numberOfUncategorizedDocuments()
            result
        }

        DocumentUI() {
            repository = documents
            editForm = new UI<Document>.EditForm() {

                @Override
                def getPredefined() {
                    getDocumentCategories()
                }
            }

        }

        /**
         * the user can delete the selected file from a case
         * the user is prompted and has to confirm the action
         */
        def markAsDeleted(boolean confirm) {
            def document = listView.selected as Document
            if (confirm) {
                delete(document.id)
            }
        }

        /**
         * the user can choose to download the selected document for viewing
         */
        def download() {
            def document = listView.selected as Document
            downloadFile(document.id)
        }

    }

    /**
     * the module contains a UI to view all documents
     * with the standard filter/pagination/sort capabilities
     */
    DocumentUI ui = new DocumentUI()

    /**
     * the module configuration
     *      filestore.location.import - the file URL pointing to the 'import' folder
     *      filestore.location.documents - the file URL pointing to the 'document-store' folder
     *      document.categories - a resource bundle/map of names pointing to category values
     */
    Map<String, Object> configuration = [:]

    /**
     * externally visible operation to create a new document
     * used by the DiagnosisModule for new reports
     * @param caseNumber
     * @param category
     * @param content the document content
     * @return the new document
     */
    def createDocument(String caseNumber, String category, def content) {
        String filename = createFileName(caseNumber, category)
        storeDocument(caseNumber, category, filename, content)
    }

    /**
     * @param n
     */
    def runPeriodicImportScans(int n = 1) {
        //Thread.start {
            int latencyInSeconds = configuration.get('check.latency.seconds') as Integer

            for (int i = 1; i <= n; i++) {
                checkAndImportFiles()
                //sleep(1000 * latencyInSeconds);
            }
        //}
    }

    /**
     * @param caseNumber
     * @param category
     * @param content the document content
     * @return the new document
     */
    private def storeDocument(String caseNumber, String category, String filename, def content) {
        def path = configuration['filestore.location.documents'] as String
        documentStore.createDirectory(path, caseNumber)
        path = "$path/$caseNumber"
        documentStore.writeFile(path, filename, content)
        def hash = calculateHash(content)
        documents.add(new Document(path: path, fileName: filename, caseNumber: caseNumber, categories: category, hash: hash))
    }

    /**
     * the module must know how to create a file name for a given case number and document category
     * @param caseNumber
     * @param category
     * @return the filename
     */
    private static String createFileName(String caseNumber, String category) {
        "${caseNumber}_$category"
    }

    /**
     */
    private def delete(String documentId) {
        Document document = documents.findById(documentId)
        document.deleted = true
        documentStore.deleteFile(document.path, document.fileName)
    }

    /**
     * service operation for UI: download a file from the filestore with given pathname
     * @param filename
     * @return the content of the file as a HTTP download
     */
    private def downloadFile(String documentId) {
        Document document = documents.findById(documentId)
        return documentStore.readFile(document.path, document.fileName)
    }

    /**
     * service operation for UI: get the configured document categories
     * @return a map of
     */
    private def getDocumentCategories() {
        return configuration['document.categories']
    }

    /**
     * service operation for UI: get number of uncategorized documents
     * @return
     */
    private int numberOfUncategorizedDocuments() {
        return documents.store.values().findAll { Document doc -> doc.categories == '-'}.size()
    }

    /**
     * the module must calculate a content hash for a given content
     */
    private static def calculateHash(def content) {
        content.hashCode()
    }

    /**
     * the module must import new files from the
     */
    private def checkAndImportFiles() {
        def folder = configuration['filestore.location.import'] as String
        def files = [:]
        files << importStore.listDirectory(folder)
        files.each { String filename, def content ->
            String caseNumber = extractCaseNumber(filename)
            storeDocument(caseNumber, '-', filename, content)
            importStore.deleteFile(folder, filename)
        }
    }

    /**
     * the module must know how to extract the case number from an import filename
     * @param filename
     * @return the case number
     */
    private static String extractCaseNumber(String filename) {
        return filename.substring(0, filename.indexOf('-'))
    }
}

