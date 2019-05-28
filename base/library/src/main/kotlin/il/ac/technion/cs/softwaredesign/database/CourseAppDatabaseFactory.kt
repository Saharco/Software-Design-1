package il.ac.technion.cs.softwaredesign.database

import il.ac.technion.cs.softwaredesign.storage.SecureStorageFactory

/**
 * Implementation of [DatabaseFactory].
 *
 * This class produces new [CourseAppDatabase] instances
 */
class CourseAppDatabaseFactory(private val storageFactory : SecureStorageFactory) : DatabaseFactory {
    override fun open(dbName: String): Database {
        val db = storageFactory
                .open(dbName.toByteArray())
        return CourseAppDatabase(db)
    }
}
