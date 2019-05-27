package il.ac.technion.cs.softwaredesign.database.mocks

import il.ac.technion.cs.softwaredesign.storage.SecureStorage

class SecureStorageMock : SecureStorage {

    private val db = mutableMapOf<String, String>()
    private val charset = charset("UTF-8")

    override fun read(key: ByteArray): ByteArray? {
        return db[key.toString(charset)]?.toByteArray()
    }

    override fun write(key: ByteArray, value: ByteArray) {
        db[key.toString(charset)] = value.toString(charset)
    }
}