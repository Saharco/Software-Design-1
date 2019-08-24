package il.ac.technion.cs.softwaredesign.mocks

import il.ac.technion.cs.softwaredesign.storage.SecureStorage

class SecureStorageMock : SecureStorage {

    private val db = mutableMapOf<String, String>()
    private val charset = charset("UTF-8")

    override fun read(key: ByteArray): ByteArray? {
        val value = db[key.toString(charset)]?.toByteArray()
        Thread.sleep(value?.size?.toLong() ?: 0) // sleep for 1ms/byte to simulate SecureStorage
        return db[key.toString(charset)]?.toByteArray()
    }

    override fun write(key: ByteArray, value: ByteArray) {
        db[key.toString(charset)] = value.toString(charset)
    }
}