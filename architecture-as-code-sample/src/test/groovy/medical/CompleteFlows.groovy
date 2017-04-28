package medical

import medical.external.GenomicsAnalysis
import medical.external.DocumentScanner
import medical.external.FileStore
import medical.model.Document
import medical.model.Sample
import medical.model.Timestamp
import spock.lang.Shared
import spock.lang.Specification

/**
 */
class CompleteFlows extends Specification {

    @Shared documentModule = new DocumentModule()

    @Shared
    String importFolder = '/mnt/import'

    @Shared
    String casesFolder = '/mnt/documents/cases'

    @Shared
    FileStore importStore = new FileStore(files: ['mnt': ['import': [:]]])

    @Shared
    FileStore documentStore = new FileStore(files: ['mnt': ['documents': ['cases' : [:]]]])

    @Shared
    DocumentScanner scanner = new DocumentScanner(attachedFileStore: importStore, importLocation: importFolder)

    @Shared patientModule = new PatientModule()
    @Shared caseModule = new CaseModule()
    @Shared
    FileStore genomicsAnalysisFilestore = new FileStore(files: [
            'pipeline': [
                    'output': [:]
            ],
            'archive' : [:]
    ])
    @Shared genomicsAnalysis = new GenomicsAnalysis(attachedFileStore: genomicsAnalysisFilestore, outputDirectory: '/pipeline/output')
    @Shared genomicsAnalysisAdapter = new GenomicsAnalysisAdapter(fileStore: genomicsAnalysisFilestore)
    @Shared diagnosisModule = new DiagnosisModule(diagnosisDB: genomicsAnalysisAdapter.database)

    @Shared
    String caseNumber

    @Shared
    String analysisNumber

    def setupSpec() {
        patientModule.configuration = [
                'casemodule.create.lnk': { String patientID -> caseModule.newCase(patientID) }
        ]
        caseModule.configuration = [
                'analysis.create.lnk': { String caseNumber, String assay, String material -> analysisModule.newAnalysis(caseNumber, assay, material) }
        ]
        diagnosisModule.configuration = [
                'analysis.find.lnk': { String aNumber -> analysisModule.retrieveAnalysis(aNumber) }
        ]
        genomicsAnalysisAdapter.configuration = [
                'output.location'          : '/pipeline/output',
                'archive.location'         : '/archive',
                'check.latency.seconds'    : 3600,
                'diagnosis.create.lnk': { String aNumber, String caseNumber -> diagnosisModule.createDiagnosis(aNumber, caseNumber)}
        ]
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
        println()
        println "--------- CaseModule timestamps -----------"
        caseModule.timestamps.listOrdered() each { println it }
        println()
    }

    def "add a patient"() {
        setup:
        def ui = patientModule.ui

        when:
        ui.newEntry()
        ui.edit()
        and:
        ui.editForm.fields =
                ['firstName': 'Theo',
                 'lastName' : 'Schmitz',
                 'age'      : 41,
                 'gender'   : 'male']
        and:
        ui.save()
        def patientId = ui.currentRecord.id

        then:
        patientModule.patients.findById(patientId)['firstName'] == 'Theo'
    }

    def "create a case from the current patient"() {
        when: 'user enters a filter criterium lastName=Schmitz'
        patientModule.ui.searchForm.searchValues = ['lastName': 'Schmitz']

        and: 'user clicks filter'
        patientModule.ui.search()

        and: 'user selects the first item in the list view from the filter result'
        patientModule.ui.listView.select(1)

        and: 'user clicks create case'
        patientModule.createCaseFromPatient()

        and: 'we change to the case module ui'
        caseModule.ui.edit()

        and:
        caseModule.ui.editForm.fields = [
                'prefix': 'NO',
                'product': 'AnalysisPlus'
        ]

        and: 'save the case'
        caseModule.ui.save()

        and:
        def currentCase = caseModule.cases.findById(caseModule.ui.currentRecord.id)
        caseNumber = currentCase.caseNumber
        then:
        caseNumber
    }

    def "scan the documents for the current case"() {
        when: 'scan 2 documents with Case-ID labels'
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

    def "run genomics analysis" () {
        given:
        def input = [(analysisNumber): [
                    'qc': ['qc-data'],
                    'enspan': (1..10).collect { "enspan$it"},
                    'muts': (1..10).collect { "muts$it"},
                    'caseNumber': caseNumber
            ]
        ]

        and:
        def runID = '-P20-BC36-DS_S1_L1234_1111111_201514'

        when:
        genomicsAnalysis.runPipeline(runID, input)

        and:
        genomicsAnalysisAdapter.runPeriodicChecks()

        then:
        1
        //todo: what
    }

    def "start diagnosis" () {
        when:
        diagnosisModule.ui.searchForm.enterSearchValue('status', 'ready')
        diagnosisModule.ui.search()

        then:
        diagnosisModule.ui.listView.displayedRecords.size() == 1

        when:
        diagnosisModule.ui.listView.select(1)
        and:
        diagnosisModule.ui.startDiagnosis()
        then:
        diagnosisModule.ui.enspanView.displayedRecords.size() == 10
    }

}
