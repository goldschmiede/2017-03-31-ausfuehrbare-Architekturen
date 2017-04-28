package medical

import java.time.LocalDateTime

import base.UI
import base.Repository
import medical.model.Case
import medical.model.Timestamp

/**
 * represents the module for case administration
 */
class CaseModule {

    /**
     * the module is build upon a standard repository
     */
    Repository<Case> cases = new Repository<Case>() {
        @Override
        /**
         * the add operation is extended:
         * map the prefix to a category and create a case number
         */
        Case add(Case cs) {
            cs.category = mapPrefix(cs.prefix)
            cs.caseNumber = createCaseNumber(cs.prefix)
            cs = super.add(cs) as Case
            timestamps.add(new Timestamp(caseNumber: cs.caseNumber, module: 'CaseModule', eventCode: 'Case Created'))
            return cs
        }
    }

    /**
     * the module contains an additional repository to keep track of the case timestamps
     */
    Repository<Timestamp> timestamps = new Repository<Timestamp>() {
        def listOrdered() {
            store.values().sort {def a, def b -> ((a as Timestamp).timestamp <=> (b as Timestamp).timestamp) }
        }
    }

    /**
     * the module needs a slightly extended standard UI
     */
    class CaseUI extends UI<Case> {

        CaseUI() {
            this.repository = cases
            this.createFunction = {new Case()}
        }

        /**
         * UI function: the user must be able to print a barcode for the current case
         * @return
         */
        def printBarCode() {
            Case current = this.currentRecord ?: this.listView.selected
            createBarCode(current?.caseNumber)
        }

        /**
         * the patient module links to listing all cases for an existing patient
         * @param patientID - required
         */
        def listCasesForPatient(String patientID) {
            this.searchForm.searchValues = ['patientID': patientID]
            this.filter()
        }

        /**
         * the account module links to listing all cases for an existing account
         * @param accountID - required
         */
        def listCasesForAccount(String accountID) {
            this.searchForm.searchValues = ['accountID': accountID]
            this.filter()
        }

        /**
         * take the currently selected case and deep link into the analysis-module's create function via the configured hyperlink
         */
        def createAnalysisFromCase() {
            Case current = ui.listView.selected ?: ui.currentRecord
            if (current != null) {
                createAnalysis(current)
            }
        }

    }

    /**
     * the module's UI
     */
    CaseUI ui = new CaseUI()

    /**
     * the module configuration
     * the module requires the mapping from prefixes to categories to be set
     *      category.map.$prefix - a name: the category for a given prefix
     *      category.map.default - a name: the default category when no other mapping can be applied
     *      analysis.create.lnk  - deep link into the analysis module
     */
    Map<String, Object> configuration = [:]

    /**
     * service method for UI:
     * create a bar code for a case number
     * @param caseNumber
     * @return the bar code
     */
    private static def createBarCode(String caseNumber) {
        caseNumber
    }

    /**
     * the patient module needs to start creation of a case based on an existing patient
     * used for deep linking into the application
     * @param patientID - required
     */
    def newCase(String patientID) {
        this.ui.newRecord = new Case(patientID: patientID)
    }

    /**
     * take the currently selected case and deep link into the analysis-module's create function via the configured hyperlink
     */
    def createAnalysis(Case current) {
        def lnk = configuration.get('analysis.create.lnk') as Closure
        lnk(current.caseNumber, current.product, current.material)
    }

    /**
     * other modules need to notify the case module about case-related events
     */
    def notify(Timestamp timestamp) {
        timestamps.add(timestamp)
    }

    private int caseSequence = 0

    /**
     * the module knows how to create a case number from a given prefix
     * @param prefix the case prefix
     * @return a string representing the case number
     */
    private String createCaseNumber(String prefix) {
        int year2digits = LocalDateTime.now().year-2000
        caseSequence++
        return "$prefix$year2digits.$caseSequence"
    }

    /**
     * the module knows how to map a prefix to a category
     * @param prefix
     * @return the corresponding category
     */
    private String mapPrefix(String prefix) {
        return configuration["category.map.$prefix"] ?: configuration['category.map.default']
    }


}
