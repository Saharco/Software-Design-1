package il.ac.technion.cs.softwaredesign.database

import il.ac.technion.cs.softwaredesign.storage.SecureStorageFactory

/**
 * Implementation of [DatabaseFactory].
 *
 * This class produces new [SecureDatabase] instances
 */
class SecureDatabaseFactory(private val storageFactory : SecureStorageFactory) : DatabaseFactory {
    override fun open(dbName: String): Database {
        val storage = storageFactory.open(dbName.toByteArray())
        return SecureDatabase(SecureCachedStorage(storage))
    }
}
