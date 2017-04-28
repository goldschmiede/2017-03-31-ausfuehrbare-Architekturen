package medical.model

/**
 *
 */
class Sample {

    /**
     * the case id
     */
    String caseNumber

    /**
     * the id of the extraction
     */
    String extractionNumber

    /**
     * the analysis number
     */
    String analysisNumber

    /**
     * denotes the sample type
     * valid values are taken from a predefined set which can be changed or extended over time
     */
    String sampleType

    /**
     * denotes the product
     * valid values are taken from a predefined set which can be changed or extended over time
     */
    String product

    @Override
    String toString() {
        "Sample - caseNo: $caseNumber, a-No: $analysisNumber, workflow: $workflow, product: $product"
    }
}
