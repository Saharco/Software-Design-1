package il.ac.technion.cs.softwaredesign.database

/**
 * Factory for accessing statistics instances.
 */
interface StatisticsFactory {
    /**
     * Opens a given statistics storage.
     * If the given storage does not exist: a new one is created
     *
     * @param storageName: name of the storage
     * @return an empty statistics storage
     */
    fun open(storageName: String): Statistical<Int, String>
}