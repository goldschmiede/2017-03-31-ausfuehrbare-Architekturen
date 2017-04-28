package medical.external

/**
 * Represents the Genomics Analysis processing
 *
 * It receives input files from the Lab and is started manually
 *
 * The analysis run produces the following files:
 *   qc (data quality) 1 record
 *   muts (mutations, insertions and deletions detected,  sev 100) records
 *   enspan (genomic position of gene fusion candidates,  sev 100) records
 *
 * Once a run has completed it provides several output files per analysis in a specific directory of an attached filestore
 */
class GenomicsAnalysis {

    /**
     * the GenomicsAnalysis has physical access to a file storage
     */
    FileStore attachedFileStore

    /**
     * the GenomicsAnalysis is configured to write into a specific directory
     */
    String outputDirectory

    /**
     * emulate a pipeline run
     * @param input - the input of the emulated run
     *                  a map: analysis-number ->
     *                             (type -> data) (type in qc, cov, muts, cn, ccn, enspan, msi)
     */
    def runPipeline(String runID, input) {
        def fileTypes = ['qc', 'muts', 'enspan', 'pdf']

        fileTypes.each {
            attachedFileStore.createDirectory(outputDirectory, it)
        }
        input.each { String analysisNumber, data ->
            String caseNumber = data['caseNumber']
            produce(runID, analysisNumber, caseNumber, 'qc', data)
            produce(runID, analysisNumber, caseNumber, 'muts', data)
            produce(runID, analysisNumber, caseNumber, 'enspan', data)
            addBinary(runID, analysisNumber, caseNumber, '_enspan.bam')
            addBinary(runID, analysisNumber, caseNumber, '.bam')
        }
    }

    /**
     * write a txt output file to the output directory of the attached filestore
     * @param analysisNumber
     * @param type
     * @param data
     */
    private produce(String runID, String analysisNumber, String caseNumber, String type, data) {
        String filename = "${analysisNumber}-${caseNumber.replace('.', '-')}-${runID}_${type}.txt"
        attachedFileStore.writeFile("$outputDirectory/$type", filename, data[type])
    }

    private addBinary(String runID, String analysisNumber, String caseNumber, String lastPart) {
        String filename = "${analysisNumber}-${caseNumber.replace('.', '-')}-${runID}${lastPart}"
        attachedFileStore.writeFile("$outputDirectory/pdf", filename, '01010101')
    }


}
