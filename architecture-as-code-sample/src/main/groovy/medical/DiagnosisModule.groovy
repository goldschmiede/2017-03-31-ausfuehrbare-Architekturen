package medical

import base.Repository
import base.UI
import medical.external.PdfGenerator
import medical.external.Database
import medical.model.Diagnosis

/**
 * represents the module for diagnosis
 *
 * data: diagnostic edits
 * data: GenomicsAnalysis data
 *  the module expects data in the form:
 *  GenomicsAnalysisData per analysisnumber
 *      QCData, qcdataPDF, Coverage[], MutsData[], CN[], Enspan[]
 * service: prepare GenomicsAnalysis data for analysis
 * service: set analyst
 * service: get case overview - combine data from patient module, case module, analysis module
 *
 * ui: case overview header
 * ui: tab single display QC-Data
 * ui: listview coverage page, sort, filter
 * ui: pdf download for coverage
 * ui: MUTS listview with colors, IGV links, annotationDB links, annotationDB functions value
 * ui: MUTS summary custom edit list form
 * ui: Enspan listview
 * ui: Enspan summary custom edit list form (positive, comment)
 * ui: CN listview  with PDF links
 * ui: CN summary custom edit list form (positive, comment)
 *
 * create report
 * prepare report
 * finalize report
 */
class DiagnosisModule {

    /**
     * the module is build upon a  repository
     */
    Repository<Diagnosis> diagnoses = new Repository<Diagnosis>()

    /**
     * database contains all GenomicsAnalysisData entries per analysis-number
     */
    Database diagnosisDB

    /**
     * A Diagnosis UI is a standard UI without edit form, create function
     * additional elements:
     *  case overview header
     *  QC data: single display QC-Data/Lab Data
     * ui: pdf download for coverage
     * ui: MUTS listview with colors, IGV links, annotationDB links, annotationDB functions value
     * ui: MUTS summary custom edit list form
     * ui: Enspan listview
     * ui: Enspan summary custom edit list form (positive, comment)
     * ui: CN listview  with PDF links
     * ui: CN summary custom edit list form (positive, comment)
     */

    class DiagnosisUI extends UI<Diagnosis> {

        class DiagnosisListView extends UI.ListView {
            def markSelected() { selected['relevant'] = true}
            def openIGV() {}
            def openAnnotationDB() {}
            def openPopup() {}
        }

        EditForm caseOverview = new EditForm(readOnly: true)

        EditForm qcDataDisplay = new EditForm(readOnly: true)

        EditForm labDataDisplay = new EditForm(readOnly: true)

        ListView covView = new ListView()

        DiagnosisListView mutsView = new DiagnosisListView()

        DiagnosisListView enspanView = new DiagnosisListView()

        DiagnosisListView cnView = new DiagnosisListView()

        DiagnosisListView ccnView = new DiagnosisListView()

        DiagnosisListView msiView = new DiagnosisListView()

        DiagnosisUI() {
            editForm = null
            repository = diagnoses
        }

        def startDiagnosis() {
            if ( listView.selected ) {
                currentRecord = listView.selected
                caseOverview.fields = [
                        'patientLink': 'diagnosis.patientData.initials',
                        'caseLink': 'diagnosis.patientData.patientAge',
                        'historyLink': currentRecord.analysisNumber,
                        'caseNumber': currentRecord.caseNumber,
                        'tumor': 'diagnosis.patientData.tumor'
                ]
                qcDataDisplay.fields = retrieveQCData(currentRecord.analysisNumber)
                mutsView.displayedRecords = retrieveMutsData(currentRecord.analysisNumber)
                enspanView.displayedRecords = retrieveEnspanData(currentRecord.analysisNumber)
            }
        }

        def createPreliminaryReport() {}

        def createFinalReport() {}
    }

    /**
     * the module has a Diagnosis UI
     */
    DiagnosisUI ui = new DiagnosisUI()

    /**
     * the module configuration
     */
    Map<String, Object> configuration = [:]

    //WordGenerator wordGenerator

    PdfGenerator pdfGenerator

    Diagnosis createDiagnosis(String analysisNumber, String caseNumber) {
        Diagnosis diagnosis = new Diagnosis(analysisNumber: analysisNumber, caseNumber: caseNumber)
        diagnoses.add(diagnosis)
    }

    def createPreliminaryReport(def templateName) {
        Diagnosis diagnosis = ui.currentRecord
        def report = pdfGenerator.generatePdf(templateName, diagnosis.properties)
        def createDocumentLnk = configuration.get('document.create.lnk') as Closure
        def category = configuration.get('document.category.preliminary-report')
        diagnosis.preliminaryReportId = createDocumentLnk(diagnosis.caseNumber, category, report)
        return report
    }

    def createFinalReport(def templateName) {
        Diagnosis diagnosis = ui.currentRecord
        def getDocumentContentLnk = configuration.get('document.content.lnk') as Closure
        def preliminaryReport = getDocumentContentLnk(diagnosis.preliminaryReportId)
        def report = pdfGenerator.generatePdf(preliminaryReport)
        def createDocumentLnk = configuration.get('document.create.lnk') as Closure
        def category = configuration.get('document.category.preliminary-report')
        diagnosis.preliminaryReportId = createDocumentLnk(diagnosis.caseNumber, category, report)
        return report
    }

    private retrieveMutsData(String analysisNumber) {
        diagnosisDB.tables['muts'].query('analysisNumber', analysisNumber)
    }

    private retrieveEnspanData(String analysisNumber) {
        diagnosisDB.tables['enspan'].query('analysisNumber', analysisNumber)
    }

    private retrieveQCData(String analysisNumber) {
        diagnosisDB.tables['qc'].query('analysisNumber', analysisNumber)?.first()?.properties
    }

}
