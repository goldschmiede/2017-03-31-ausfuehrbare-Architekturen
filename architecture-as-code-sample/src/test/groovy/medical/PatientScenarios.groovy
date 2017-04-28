package medical

import spock.lang.Shared
import spock.lang.Specification

/**
 */
class PatientScenarios extends Specification {

    @Shared patientModule = new PatientModule()

    def "add a patient" () {
        setup:
        def ui = patientModule.ui

        expect:
        patientModule.patients.countEntries() == 0

        when:
        ui.newEntry()
        ui.edit()
        and:
        ui.editForm.fields =
                ['firstName': 'Theo',
                 'lastName': 'Schmitz',
                 'age': 41,
                 'gender': 'male']
        and:
        ui.save()

        then:
        patientModule.patients.countEntries() == 1

        when:
        ui.searchForm.searchValues =
                ['firstName': 'T']
        and:
        ui.search()

        then:
        ui.listView.displayedRecords.size() == 1
        and:
        ui.listView.displayedRecords.first()['lastName'] == 'Schmitz'
    }

    def "modify a patient" () {
        setup:
        def ui = patientModule.ui

        expect:
        patientModule.patients.countEntries() == 1

        when:
        ui.search()

        then:
        ui.listView.displayedRecords.size() == 1

        when:
        ui.listView.select(1)
        and:
        ui.edit()
        and:
        ui.editForm.fields =
                ['lastName': 'Schmidt']
        and:
        ui.save()

        then:
        patientModule.patients.countEntries() == 1

        when:
        ui.searchForm.searchValues =
                ['firstName': 'T']
        and:
        ui.search()

        then:
        ui.listView.displayedRecords.size() == 1
        and:
        ui.listView.displayedRecords.first()['lastName'] == 'Schmidt'
    }
}
