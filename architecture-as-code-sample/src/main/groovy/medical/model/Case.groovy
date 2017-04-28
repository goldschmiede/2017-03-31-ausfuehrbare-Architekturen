package medical.model

import java.time.LocalDateTime

import base.Entity

/**
 * represents a case
 */
class Case extends Entity {

    /**
     * a case is usually assigned to a patient
     */
    String patientID

    /**
     * a case has a special identifier which is structured in a specific way
     * prefix, year as 2 digits, a '.' and a sequential number, e.g. NO16.1234
     */
    String caseNumber

    /**
     * flexible classification; possible values potentially change over time
     */
    String category

    /**
     * flexible classification; possible values potentially change over time
     */
    String prefix

    /**
     * ordered assay
     */
    String product

    /**
     * the sample type / type of material
     * FFPE     - FFPE pathology and extraction
     * cfDNA    - cdDNA extraction, e.g. blood
     * genomic  - genomic DNA extraction, e.g. spinal fluid
     * external - external DNA sample processing
     * research -
     */
    //    enum SampleType { none, FFPE, cfDNA, genomic, external, research }

    /**
     * Sample type, e.g. FFPE
     */
    String material

    String researchProject
    String billingInformation

    LocalDateTime timestampCreated = LocalDateTime.now()
    LocalDateTime sampleReceived

    /*----------------------------------------------------------------------------------*/
    @Override
    String toString() {
        return "Case - caseNumber: $caseNumber, patientID: $patientID, prefix: $prefix, category: $category"
    }

}
