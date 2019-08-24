package il.ac.technion.cs.softwaredesign.database

import il.ac.technion.cs.softwaredesign.storage.SecureStorage

/**
 * Implementation of [Database].
 *
 * @see SecureDatabaseFactory
 */
class SecureDatabase(private val storage: SecureStorage) : Database {
    private val path = "/"

    override fun collection(name: String): CollectionReference {
        return object : SecureCollectionReference(
                path + name.replace("/", ""), storage) {}

    }
}