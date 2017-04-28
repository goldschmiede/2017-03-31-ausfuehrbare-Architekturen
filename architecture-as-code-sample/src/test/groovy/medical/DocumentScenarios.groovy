package medical

import medical.external.DocumentScanner
import medical.external.FileStore
import medical.model.Document
import spock.lang.Shared
import spock.lang.Specification

/**
 * user oriented tests of document-related actions
 *
 * 1. import several files
 *      wait for periodic import scan
 *      expect:
 *          files show up as documents in the uncategorized list
 *          files are removed from the import folder
 *          files are stored in the document filestore in case-specific folders
 * 2.  assign a category to a document
 *      expect:
 *          document does not show up in the uncategorized list
 *          document shows up in the list of case documents
 */
class DocumentScenarios extends Specification {

    @Shared
    // the DocumentModule is the central object under test
    def documentModule = new DocumentModule()

    @Shared
    // configuration of the filestore
    String importFolder = '/mnt/import'

    @Shared
    // configuration of the filestore
    String casesFolder = '/mnt/documents/cases'

    @Shared
    // layout of the filestore for scanner import
    FileStore importStore = new FileStore(files: ['mnt': ['import': [:]]])

    @Shared
    // layout of the filestore used by the document module
    FileStore documentStore = new FileStore(files: ['mnt': ['documents': ['cases' : [:]]]])

    @Shared
    // we use one scanner and attach it to the filestore
    DocumentScanner scanner = new DocumentScanner(attachedFileStore: importStore, importLocation: importFolder)

    /**
     * setup: configure the document module with the filestores and its layout
     */
    def setupSpec() {
        documentModule.configuration = [
                'filestore.location.import': importFolder,
                'filestore.location.documents' : casesFolder,
                'check.latency.seconds': 60,
                'document.categories': ['unassigned': '-', 'Patient Consent': 'consent', 'Medical History': 'history']
        ]
        documentModule.documentStore = documentStore
        documentModule.importStore = importStore
    }

    def cleanupSpec() {
    }

    /*--------------------------------------------------------------------------
     test cases
     --------------------------------------------------------------------------*/

    /**
     * new document scans with case labels are displayed in the document module's overview
     */
    def "scan documents with case labels and show in case documents view"() {
        when: 'assume a case number'
        def caseNumber = "NO16.444"

        and: 'scan 2 documents with Case-ID labels'
        scanner.scan('contentH2812103', caseNumber)
        scanner.scan('contentH2812104', caseNumber)

        and: 'filter by casenumber'
        documentModule.ui.searchForm.enterSearchValue('caseNumber', caseNumber)
        documentModule.ui.search()
        then: 'the document module shows nothing'
        documentModule.ui.listView.displayedRecords.size() == 0

        when: 'import scan is running'
        documentModule.runPeriodicImportScans(1)

        and:  'we filter for documents without category'
        documentModule.ui.searchForm.enterSearchValue('categories', '-')
        documentModule.ui.search()

        then:  'found 2'
        def docs = documentModule.ui.listView.displayedRecords
        docs.size() == 2

        and:   'documents are not yet categorized'
        docs.every {
            Document doc -> doc.categories == '-'
        }

        and:   'documents have correct metadata'
        docs.every {
            Document doc ->
                doc.caseNumber == caseNumber &&
                doc.hash != 0
        }

        and:   'displayed number of uncategorized documents is correct'
        documentModule.ui.nDocs == 2
    }

    /**
     * assign a category to a document
     */
    def "assign a category"() {
        given: 'use the last case number'
        def caseNumber = "NO16.444"

        when:  'we filter for documents assigned to the given casenumber'
        documentModule.ui.searchForm.enterSearchValue('caseNumber', caseNumber)
        documentModule.ui.search()

        then:  'found 2'
        def docsForCase = documentModule.ui.listView.displayedRecords
        docsForCase.size() == 2

        when:  'we assign a category to both documents'
        documentModule.ui.listView.select(1)
        documentModule.ui.edit()
        def cat = documentModule.ui.editForm.predefined['Patient Consent']
        documentModule.ui.editForm.enterFieldValue('categories', cat)
        documentModule.ui.save()
        documentModule.ui.listView.select(2)
        documentModule.ui.edit()
        documentModule.ui.editForm.enterFieldValue('categories', cat)
        documentModule.ui.save()

        then:   'documents are categorized'
        docsForCase.every {
            Document doc -> doc.categories == 'consent'
        }

        and:   'displayed number of uncategorized documents is correct'
        documentModule.ui.nDocs == 0
    }

    def "list all documents for a case"() {
        when:
        def caseNumber = "NO16.556"
        scanner.scan('556-1', caseNumber)
        scanner.scan('556-2', caseNumber)

        and:
        documentModule.runPeriodicImportScans(1)

        and:
        documentModule.ui.searchForm.enterSearchValue('caseNumber', caseNumber)
        documentModule.ui.search()

        then:
        documentModule.ui.listView.displayedRecords.size() == 2

        when:
        documentModule.ui.listView.select(1)
        and:
        def doc = documentModule.ui.listView.selected as Document

        then:
        doc.fileName.contains('Scan-')
        doc.fileName.contains(caseNumber)
        doc.fileName.endsWith('.pdf')
        and:
        doc.caseNumber == caseNumber
        and:
        "$casesFolder/$caseNumber" as String == doc.path
    }
}
