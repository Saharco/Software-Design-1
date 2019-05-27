package il.ac.technion.cs.softwaredesign.database

import il.ac.technion.cs.softwaredesign.storage.SecureStorageFactory

class CourseAppDatabaseFactory(private val storageFactory : SecureStorageFactory) : DatabaseFactory {
    override fun open(dbName: String): Database {
        val db = storageFactory
                .open(dbName.toByteArray())
        return CourseAppDatabase(db)
    }
}
