package medical

import medical.external.FileStore
import medical.external.Database
import medical.model.Timestamp

/**
 * The GenomicsAnalysisAdapter imports the results of a genomics analysis rund into an internal database
 *
 * the pipeline writes several files per analysis to a filestore
 * - filename contains analysis-number + case number + file type
 * - format is tab-separated data with column headers in text files and pdf
 * - different directories per file type
 *
 * The component
 *  - checks the pipeline output periodically
 *  - imports new data into the database
 *  - know about file structure (column headers) and filename structure
 *  - notifies the analysis module about imported data per analysis number
 */
class GenomicsAnalysisAdapter {

    /**
     * the configuration of the GenomicsAnalysis
     * the GenomicsAnalysis has to be configured with the pipeline output files location
     *      output.location - file URL: this is the base URL pointing to the output files
     *      archive.location - file URL: this is the base URL pointing to the archived files
     *      check.latency.seconds - integer value w r to unit seconds: how often does the component check if there are output files to import
     *      analysis.notification.lnk - URL: link to notify the analysis module about a new event (timestamp)
     *      analysis.completed.lnk - URL: link to notify the analysis module about completion of a comp bio run
     */
    def configuration = [:]

    /**
     * the GenomicsAnalysis pipeline produces output files which are attached to the adapter
     */
    FileStore fileStore

    /**
     * the adapter must know about the file types which must map to output directories and file names
     */
    def fileTypes = ['qc', 'muts', 'enspan']

    /**
     * the internal database schema creation - one table for each filetype
     */
    private createSchema = {
        fileTypes.collectEntries { String name -> [(name): new Database.Table(name: name)] }
    }

    /**
     * the internal database
     */
    Database database = new Database(tables: createSchema())


    // cache from configuration
    private createDiagnosisLink
    private baseDirectory

    /**
     * the component shields the outside world from the fact that the internal pipeline
     * produces output files without further notification
     *
     * the end of a pipeline run can only be detected by periodic polling
     * @param n
     */
    def runPeriodicChecks(int n = 1) {
        //Thread.start {
            int latencyInSeconds = configuration.get('check.latency.seconds') as Integer
            createDiagnosisLink = configuration.get('diagnosis.create.lnk') as Closure
            baseDirectory = configuration['output.location']

            for (int i = 1; i <= n; i++) {
                checkAndImportFiles()
                //sleep(1000 * latencyInSeconds);
            }
        //}
    }

    /**
     * the module must handle the file or files for a given analysis by the following procedure:
     *  (1) mark files as being processed
     *  (2) import file data into database
     *  (3) move files to archive location
     *  (4) notify analysis module: timestamp/import completed
     *  todo: links to PDF files
     */
    def checkAndImportFiles() {
        def processedEntries = [:]

        if ( fileStore.listDirectory("$baseDirectory/qc") ) {
            fileTypes.each { String fileType ->
                processedEntries << importFiles(fileType)
            }
        }
        processedEntries.each { String analysisNumber, String caseNumber ->
            if ( isAnalysisComplete(analysisNumber) ) {
                createDiagnosisLink(analysisNumber, caseNumber)
            }
        }
    }

    /**
     * the component must know how to identify completed runs by finding specific files
     * the component must know how to extract the analysis-number from the filenames
     * @return a list of analysis-numbers that are ready for importing
     */
    private importFiles(String dataType) {
        def processedEntries = [:]
        String dir = "$baseDirectory/$dataType"
        String archiveDir = "/archive"
        def files = [:]
        files << fileStore.listDirectory(dir)
        files.each { filename, handle ->
            def fn = filename as String
            def analysisNumber = extractAnalysisNumber(fn)
            def caseNumber = extractCaseNumber(fn)
            def content = fileStore.readFile(dir, fn)
            writeToDatabase(dataType, analysisNumber, content)
            fileStore.moveFile(dir, archiveDir, fn)
            processedEntries[analysisNumber] = caseNumber
        }
        processedEntries
    }

    private void writeToDatabase(String type, String analysisNumber, content) {
        Database.Table table = database.tables[type]
        content.each {
            table.data << ['analysisNumber': analysisNumber, 'rowdata': it]
        }
    }

    /**
     * the module must check if the data for an analysisNumber is complete
     * an analysis is complete if there is data imported for every file type
     * @param analysisNumber
     * @return true/false
     */
    private isAnalysisComplete = { String analysisNumber ->
        fileTypes.every { String type ->
            Database.Table table = database.tables[type]
            table.data.find { row -> row['analysisNumber'] == analysisNumber }
        }
    }

    /**
     * the module must know how to extract an analysis number for a given pipeline output file
     * @param filename
     * @return the analysis number
     */
    private extractAnalysisNumber = { String filename -> filename.substring(0, filename.indexOf('-')) }

    /**
     * the module must know how to extract a case number for a given pipeline output file
     * @param filename
     * @return the case number
     */
    private extractCaseNumber = { String filename ->
        def four_parts = filename.split('-', 4)
        return "${four_parts[1]}.${four_parts[2]}"
    }

}
