package medical

import base.Repository
import base.UI
import medical.model.Patient

/**
 * represents the module for patient administration
 */
class PatientModule {

    /**
     * the module is based on a standard repository
     */
    Repository<Patient> patients = new Repository<>()

    /**
     * the module contains a standard UI
     * overview with filter, sort, page
     * details with create, edit, save
     */
    UI<Patient> ui = new UI<Patient>(repository: patients, createFunction: { new Patient() })

    /**
     * the module configuration
     * the module requires the deep link into the case module to be configured:
     *      casemodule.create.lnk - a hyperlink into the casemodule create case function, must be complemented with a parameter 'patientID'
     *      casemodule.list.lnk - a hyperlink into the casemodule overview, must be complemented with a parameter 'patientID'
     *      medicalhistorymodule.create.lnk - a hyperlink into the module create medical history entry function, must be complemented with a parameter 'patientID'
     *      medicalhistorymodule.list.lnk - a hyperlink into the medical history overview, must be complemented with a parameter 'patientID'
     */
    Map<String, Object> configuration = [:]

    /*----------------------------------------------------------------------------------*/

    /**
     * take the currently selected patient and deep link into the casemodule via the configured hyperlink
     */
    def createCaseFromPatient() {
        Patient current = ui.listView.selected ?: ui.currentRecord
        if (current != null) {
            def lnk = configuration.get('casemodule.create.lnk') as Closure
            lnk(current.id)
        }
    }

    /**
     * take the currently selected patient and deep link into the casemodule overview via the configured hyperlink
     */
    def listCasesForPatient() {
        Patient current = ui.listView.selected
        if (current != null) {
            def lnk = configuration.get('casemodule.list.lnk') as Closure
            lnk(current.id)
        }
    }

    /**
     * take the currently selected patient and deep link into the medical history module via the configured hyperlink
     */
    def createMedicalHistoryFromPatient() {
        Patient current = ui.listView.selected
        if (current != null) {
            def lnk = configuration.get('medicalhistorymodule.create.lnk') as Closure
            lnk(current.id)
        }
    }

    /**
     * take the currently selected patient and deep link into the medical history module via the configured hyperlink
     */
    def listMedicalHistoryForPatient() {
        Patient current = ui.listView.selected
        if (current != null) {
            def lnk = configuration.get('medicalhistorymodule.list.lnk') as Closure
            lnk(current.id)
        }
    }

}
