package medical.model

import java.time.LocalDateTime

import base.Entity

/**
 *
 */
class Timestamp extends Entity {
    LocalDateTime timestamp = LocalDateTime.now()
    String caseNumber
    String analysisNumber
    String module
    String eventCode
    String info

    @Override
    String toString() {
        "Timestamp - $timestamp, caseNo: $caseNumber, a-No: $analysisNumber, module: $module, event: $eventCode, info: $info"
    }
}