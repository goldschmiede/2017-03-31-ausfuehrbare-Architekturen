package base

/**
 * Base class for repositories (DDD)
 */
class Repository<T extends Entity> {

    Map<String, T> store = new HashMap<>()

    T findById(String id) {
        store.get(id)
    }

    T add(T entity) {
        store.put(entity.id, entity)
        return entity
    }

    T update(T entity) {
        store.put(entity.id, entity)
        return entity
    }

    int countEntries() {
        return store.size()
    }

    def filter(String filter) {
        def all = store.values()
        return all.findAll { it.toString() contains filter }
    }

    def search(Map<String, String> filter) {
        def all = store.values()
        return filter ? all.findAll {
            T record ->
                filter.every { key, value ->
                    record.getProperty(key as String)?.toString()?.contains(value)
                }
        } : all
    }

    def listAll() {
        return store.values()
    }

}
