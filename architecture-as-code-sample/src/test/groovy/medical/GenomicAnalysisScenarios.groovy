package medical

import medical.external.GenomicsAnalysis
import medical.external.Database
import medical.external.FileStore
import spock.lang.Shared
import spock.lang.Specification

/**
 */
class GenomicAnalysisScenarios extends Specification {

    @Shared diagnosisModule = new DiagnosisModule()

    @Shared
    FileStore genomicsAnalysisFilestore = new FileStore(files: [
            'pipeline': [
                    'output': [:]
            ],
            'archive': [:]
    ])
    @Shared genomicsAnalysis = new GenomicsAnalysis(attachedFileStore: genomicsAnalysisFilestore, outputDirectory: '/pipeline/output')
    @Shared genomicsAnalysisAdapter = new GenomicsAnalysisAdapter(fileStore: genomicsAnalysisFilestore)

    def setupSpec() {
        genomicsAnalysisAdapter.configuration = [
                'output.location': '/pipeline/output',
                'archive.location': '/archive',
                'check.latency.seconds': 3600,
                'diagnosis.create.lnk': { String aNumber, String caseNumber -> diagnosisModule.createDiagnosis(aNumber, caseNumber)}
        ]
    }

    def "run genomics analysis" () {
        given:
        def caseNumber = 'PO16.123'

        and:
        def input = [:]
            input[caseNumber] = [
                    'qc': ['qc-data'],
                    'enspan': (1..10).collect { "enspan$it"},
                    'muts': (1..10).collect { "muts$it"},
                    'caseNumber': caseNumber
            ]

        and:
        def runID = '-P20-BC36-DS_S1_L1234_1111111_201514'

        when:
        genomicsAnalysis.runPipeline(runID, input)

        then:
        genomicsAnalysisFilestore.listDirectory('/pipeline/output/muts').size() == 1

        and:
        genomicsAnalysisFilestore.listDirectory('/pipeline/output/enspan').size() == 1

        when:
        genomicsAnalysisAdapter.runPeriodicChecks()

        then:
        dataComplete(caseNumber, genomicsAnalysisAdapter.database)
    }

    private static boolean dataComplete (def caseNumber, Database database)  {
        def records = database.tables['enspan'].query('analysisNumber', caseNumber)
        def firstCN = records[0]
        records = database.tables['muts'].query('analysisNumber', caseNumber)
        def firstMuts = records[0]
        firstMuts['rowdata'] == 'muts1'
    }
}

