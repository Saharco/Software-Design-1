package il.ac.technion.cs.softwaredesign.database

import il.ac.technion.cs.softwaredesign.database.datastructures.AVLTree
import il.ac.technion.cs.softwaredesign.storage.SecureStorage

class SecureStatistics(storage: SecureStorage) : Statistical<Int, String> {

    private val storageMap = AVLTree(storage, ::compareKeys)
    private val charset = Charsets.UTF_8

    override fun update(value: String, newPrimaryKey: Int, oldPrimaryKey: Int, secondaryKey: Int, isDelete: Boolean) {
        val prevKey = generateKey(oldPrimaryKey, secondaryKey)
        storageMap.delete(prevKey)
        if (!isDelete) {
            val newKey = generateKey(newPrimaryKey, secondaryKey)
            storageMap.insert(newKey, value.toByteArray(charset))
        }
    }

    override fun topK(k: Int): List<String> {
        return storageMap.topK(k)
    }

    /**
     *  Generates a key from a primary key & secondary key
     */
    private fun generateKey(primaryKey: Int, secondaryKey: Int) =
            "$primaryKey/$secondaryKey".toByteArray(charset)

    /**
     * Compares two keys. The keys are compared such that:
     * [key1] > [key2] if its primary key is bigger, or its secondary key is smaller.
     * [key2] > [key1] if its primary key is bigger, or its secondary key is smaller.
     * A pair of keys where none of these conditions are met are considered to be equal
     *
     * @return a positive number if [key1] > [key2],
     *  negative number if [key2] > [key1]
     *  0 otherwise.
     */
    private fun compareKeys(key1: ByteArray, key2: ByteArray): Int {
        val key1String = key1.toString(il.ac.technion.cs.softwaredesign.database.datastructures.charset)
        val key2String = key2.toString(il.ac.technion.cs.softwaredesign.database.datastructures.charset)
        val key1separatorIndex = key1String.indexOf('/')
        val key2separatorIndex = key2String.indexOf('/')

        val primary1 = key1String.substring(0, key1separatorIndex).toInt()
        val secondary1 = key1String.substring(key1separatorIndex + 1).toInt()
        val primary2 = key2String.substring(0, key2separatorIndex).toInt()
        val secondary2 = key2String.substring(key2separatorIndex + 1).toInt()
        if (primary1 > primary2) {
            return 1
        } else if (primary1 < primary2) {
            return -1
        }
        if (secondary1 < secondary2)
            return 1
        else if (secondary2 < secondary1)
            return -1
        return 0
    }
}