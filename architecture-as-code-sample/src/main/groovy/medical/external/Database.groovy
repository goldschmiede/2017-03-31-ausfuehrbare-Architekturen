package medical.external

/**
 * Abstraction of a relational database
 */
class Database {

    static class Table {
        // tablename
        String name
        // tabledata as list of rows, each row is a map column-name -> value
        def data = []
        // very simple query with one column
        def query(String column, String filter) {
            data.findAll { it[column] == filter }
        }
    }

    /**
     * the tables in the database
     */
    Map<String, Table> tables = new HashMap<>()
}
