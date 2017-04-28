package medical

import medical.model.Case
import medical.model.Patient
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 */
class CaseScenarios extends Specification {

    @Shared
    def caseModule = new CaseModule()

    @Shared
    def patientModule = new PatientModule()

    def setupSpec() {
        patientModule.configuration = [
                'casemodule.create.lnk': {String patientID -> caseModule.newCase(patientID)}
        ]
        caseModule.configuration = [
                'category.map.BF': 'research',
                'category.map.NO': 'diagnostic',
                'category.map.NP': 'diagnostic',
                'category.map.default': 'other'
        ]
    }

    def cleanupSpec() {
        println()
        println "--------- CaseModule timestamps -----------"
        caseModule.timestamps.listOrdered() each { println it }
    }

    private def addPatient(String firstName, String lastName, int age, String gender) {
        return patientModule.patients.add(new Patient(firstName: firstName, lastName: lastName, age: age, gender: Patient.Gender.valueOf(gender)))
    }

    private def addCase(String patientID, String prefix) {
        return caseModule.cases.add(new Case(patientID: patientID, prefix: prefix))
    }

    private def listCases(String filter) {
        return caseModule.cases.filter(filter)
    }

    /*--------------------------------------------------------------------------
     test cases
     --------------------------------------------------------------------------*/

    def "create a case from a selected patient" () {
        setup: 'we provide 3 patients in the database with one named Schmitz'
        addPatient('Theo', 'Schmitz', 41, 'male')
        addPatient('Lisa', 'Schmidt', 50, 'female')
        addPatient('Ina', 'Müller', 39, 'female')

        when:  'user enters a filter criterium lastName=Schmitz'
        patientModule.ui.searchForm.searchValues = ['lastName': 'Schmitz']

        and:  'user clicks filter'
        patientModule.ui.search()

        and:  'user selects the first item in the list view from the filter result'
        patientModule.ui.listView.select(1)

        and:  'user clicks create case'
        patientModule.createCaseFromPatient()

        and:  'we change to the case module ui'
        caseModule.ui.edit()

        then: 'we see a new case'
        caseModule.ui.currentRecord != null

        and: 'the patient id is prefilled with the patient that was selected in the patient UI'
        caseModule.ui.currentRecord.patientID == patientModule.ui.listView.selected.id
    }

    @Unroll("case number generation for prefix: #prefix")
    def "case number is generated according to specific format rules" () {
        when: 'patient with a specific ID'
        def patientID = '1234'

        and: 'create a case for that patient'
        caseModule.ui.newCase(patientID)

        and: 'assign prefix to that case'
        caseModule.ui.edit()
        caseModule.ui.editForm.fields = [
                'prefix': prefix,
                'product': 'some'
        ]

        and: 'save the case'
        caseModule.ui.save()

        def current = caseModule.ui.currentRecord

        then: 'we have a unique casenumber assigned to that case'
        listCases(current.caseNumber) == [current]

        and: 'the casenumber starts with the prefix'
        current.caseNumber.startsWith(prefix)

        where:
        prefix << ['NO', 'BF', 'NP', 'ST']
    }

    @Unroll("automatic case category assignment for prefix: #prefix")
    def "case category is assigned automatically according to the module configuration" () {
        when: 'patient with a specific ID'
        def patientID = '1234'

        and: 'create a case for that patient'
        caseModule.ui.newCase(patientID)

        and: 'assign prefix to that case'
        caseModule.ui.edit()
        caseModule.ui.editForm.fields = [
                'prefix': prefix,
                'product': 'some'
        ]

        and: 'save the case'
        caseModule.ui.save()

        def current = caseModule.ui.currentRecord

        then: 'the case has an assigned category value'
        current.category

        and: 'the category conforms to the configured mapping'
        current.category ==
                caseModule.configuration["category.map.$prefix"] ?:
                caseModule.configuration["category.map.default"]

        where:
        prefix << ['NO', 'BF', 'NP', 'ST']
    }

    @Unroll("find cases for a patient with number of cases = #numberOfCases")
    def "find cases for a patient" () {
        when: 'a dummy patient and one prefix'
        def patient = addPatient('NN', 'NN', 50, 'male')
        def prefix = 'NO'

        and: 'add n cases for this patient'
        def cases = []
        (1..numberOfCases).each {
            cases << addCase(patient.id, prefix)
        }

        and: 'we filter in the ui for the patient id'
        def ui = caseModule.ui

        ui.searchForm.enterSearchValue('patientID', patient.id)
        ui.search()

        then: 'the filter result is not empty'
        ui.listView.displayedRecords

        and: 'the list view displays exactly those cases we just created'
        ui.listView.displayedRecords.size() == numberOfCases
        ui.listView.displayedRecords.containsAll(cases)

        where:
        numberOfCases << [1, 2, 3, 5]
    }

    // todo:

    @Ignore
    def "manage predefined lists"() {
    }

    @Ignore
    def "add account"() {
    }

    @Ignore
    def "print sample sheet"() {
    }

    @Ignore
    def "see case workflow steps"() {
    }

    @Ignore
    def "see case-related timestamps"() {
    }

    @Ignore
    def "add sample received as timestamp"() {
    }

}
