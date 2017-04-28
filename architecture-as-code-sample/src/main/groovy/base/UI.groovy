package base

/**
 * Base class for standard UIs
 */
class UI<T extends Entity> {

    /**
     * a standard UI has a search form
     */
    static class SearchForm {
        /**
         * the values entered in the search form
         * set these values, e.g.
         * ui.searchform.searchValues = ['firstName':'Tom', 'lastName':'S']
         */
        def searchValues = [:]

        /**
         * equivalent of entering one value in the search form
         */
        def enterSearchValue(String attribute, String value) {
            searchValues.put(attribute, value)
        }

        def clearSearchForm() {
            searchValues = [:]
        }
    }

    /**
     * a standard UI has a list view to display the results of a search
     */
    static class ListView {
        /**
         * the records displayed
         */
        def displayedRecords = []

        /**
         * the currently selected record
         */
        def selected

        /**
         * selecting a single record
         */
        def select(int index) {
            if (index > 0 && index <= displayedRecords.size())
                selected = displayedRecords[index-1]
        }

        def clear() {
            displayedRecords = []
            selected = null
        }
    }

    /**
     * a standard UI has an edit view
     */
    static class EditForm {

        /**
         * the values entered in the edit form
         */
        def fields = [:]

        /**
         * used for predefined/default values
         */
        def predefined = [:]

        boolean readOnly

        /**
         * emulates entering a value in the edit form
         */
        def enterFieldValue(String attribute, Object value) {
            if ( !readOnly)
                fields.put(attribute, value)
        }

        def clearForm() {
            fields = [:]
        }
    }

    // a standard UI uses a repository
    Repository<T> repository

    SearchForm searchForm = new SearchForm()

    EditForm editForm = new EditForm()

    ListView listView = new ListView()

    T currentRecord, newRecord

    /**
     * set the create function to create new objects
     */
    def createFunction

    /**
     * equivalent of submit search form
     */
    def search() {
        listView.clear()
        currentRecord = newRecord = null
        def searchResult = repository.search(searchForm.searchValues)
        listView.displayedRecords = searchResult
    }

    /**
     * equivalent of activating the edit form
     */
    def edit() {
        if (listView.selected)
            currentRecord = listView.selected
        else if ( newRecord )
            currentRecord = newRecord
    }

    /**
     * equivalent of submit edit form
     */
    def save() {
        editForm.fields.each { def field -> currentRecord.setProperty(field.key as String, field.value) }
        if ( newRecord?.id == currentRecord.id )
            currentRecord = repository.add(currentRecord)
        else
            currentRecord = repository.update(currentRecord)
    }

    def newEntry() {
        currentRecord = null
        listView.selected = null
        newRecord = createFunction()
    }

}
