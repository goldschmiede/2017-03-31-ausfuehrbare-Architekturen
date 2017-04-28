package base

import medical.model.Patient
import spock.lang.Specification

class UITests extends Specification {

    def "UI works as expected" () {
        given:
        def repository = new Repository<Patient>()
        def ui = new UI<Patient>(repository: repository)
        def theo = repository.add(new Patient(firstName: 'Theo', lastName: 'Schmitz', age:41, gender: Patient.Gender.male))
        def lisa = repository.add(new Patient(firstName: 'Lisa', lastName: 'Schmitz', age:40, gender: Patient.Gender.female))

        when:
        ui.search()

        then:
        ui.listView.displayedRecords.size() == 2

        and:
        ui.listView.displayedRecords.contains(theo)

        and:
        ui.listView.displayedRecords.contains(lisa)

        when:
        ui.listView.select(1)

        and:
        ui.edit()

        and:
        ui.editForm.enterFieldValue('firstName', 'Lise')

        and:
        ui.save()

        and:
        ui.searchForm.enterSearchValue('firstName', 'L')

        and:
        ui.search()

        then:
        ui.listView.displayedRecords.size() == 1

        and:
        ui.listView.displayedRecords[0]['firstName'] == 'Lise'
    }
}
