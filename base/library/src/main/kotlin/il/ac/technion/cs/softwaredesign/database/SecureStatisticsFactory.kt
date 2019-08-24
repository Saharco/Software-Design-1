package il.ac.technion.cs.softwaredesign.database

import il.ac.technion.cs.softwaredesign.storage.SecureStorageFactory

class SecureStatisticsFactory(private val storageFactory : SecureStorageFactory) : StatisticsFactory {
    override fun open(storageName: String): Statistical<Int, String> {
        val storage = storageFactory.open(storageName.toByteArray())
        return SecureStatistics(SecureCachedStorage(storage))
    }
}