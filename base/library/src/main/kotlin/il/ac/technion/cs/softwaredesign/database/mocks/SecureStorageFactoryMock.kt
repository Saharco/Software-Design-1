package il.ac.technion.cs.softwaredesign.database.mocks

import il.ac.technion.cs.softwaredesign.storage.SecureStorage
import il.ac.technion.cs.softwaredesign.storage.SecureStorageFactory

class SecureStorageFactoryMock : SecureStorageFactory {

    private val existingDatabases = mutableMapOf<String, SecureStorage>()

    override fun open(name: ByteArray): SecureStorage {
        val dbName = String(name)
        return if (existingDatabases.containsKey(dbName)) {
            existingDatabases[dbName]!!
        } else {
            val storage = SecureStorageMock()
            existingDatabases[dbName] = storage
            storage
        }
    }
}