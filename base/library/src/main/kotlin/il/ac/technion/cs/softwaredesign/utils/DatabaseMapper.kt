package il.ac.technion.cs.softwaredesign.utils

import il.ac.technion.cs.softwaredesign.database.SecureCachedStorage
import il.ac.technion.cs.softwaredesign.database.Database
import il.ac.technion.cs.softwaredesign.database.Statistical

/**
 * Wrapper class that maps database names to their respective databases and storage name to their respective statistics
 * @param dbMap: String->Database map
 * @param statisticsMap: String->Statistical<Int, String> map
 */
class DatabaseMapper(private val dbMap: Map<String, Database>,
                     private val statisticsMap: Map<String, Statistical<Int, String>>) {

    /**
     * @return the Database instance that [dbName] is being mapped to
     */
    fun getDatabase(dbName: String): Database {
        return dbMap.getValue(dbName)
    }

    /**
     * @return the SecureCachedStorage instance that [storageName] is being mapped to
     */
    fun getStatistics(storageName: String): Statistical<Int, String> {
        return statisticsMap.getValue(storageName)
    }
}