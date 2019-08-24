package il.ac.technion.cs.softwaredesign.database.datastructures

/**
 * Represents a dictionary data structure
 */
interface Dictionary<K, V> {
    /**
     * Inserts [value] into the dictionary with the key [key]
     */
    fun insert(key: K, value: V)
    /**
     * Deletes the value in the dictionary that corresponds to key [key]
     */
    fun delete(key: K)
}