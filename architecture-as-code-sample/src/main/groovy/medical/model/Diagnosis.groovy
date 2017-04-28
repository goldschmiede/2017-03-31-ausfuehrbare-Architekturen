package medical.model

import java.time.LocalDateTime

import base.Entity

/**
 * represents a diagnosis
 */
class Diagnosis extends Entity {

    String patientID

    String caseNumber

    String analysisNumber

    String preliminaryReportId

    enum Status { ready, started, review, submitted }

    LocalDateTime timestampCreated = LocalDateTime.now()

    Status status = Status.ready

    /*----------------------------------------------------------------------------------*/
    @Override
    String toString() {
        return "Diagnosis - caseNumber: $caseNumber, patientID: $patientID, status: $status"
    }

}
