package il.ac.technion.cs.softwaredesign.database

import il.ac.technion.cs.softwaredesign.database.datastructures.LimitedCacheMap
import il.ac.technion.cs.softwaredesign.storage.SecureStorage
import java.util.concurrent.CompletableFuture

/**
 * Wraps a SecureStorage instance. Provides caching for read operations
 * @param storage: storage to wrap
 * @param limit: maximum size of the cache
 */
class SecureCachedStorage(private val storage: SecureStorage, limit: Int = 18000): SecureStorage {

    private val cache = LimitedCacheMap<String, ByteArray?>(limit)

    var cacheLimit: Int = limit
        set(newLimit) {
            cache.limit = newLimit
            field = newLimit
        }

    override fun read(key: ByteArray): ByteArray? {
        val keyString = String(key)
        if (cache.containsKey(keyString))
            return cache[keyString]
        val readValue = storage.read(key)
        cache[keyString] = readValue
        return readValue
    }

    override fun write(key: ByteArray, value: ByteArray){
        storage.write(key, value)
        cache[String(key)] = value
    }
}