package il.ac.technion.cs.softwaredesign.database

/**
 * Represents a queriable data-store whose values can be updated
 */
interface Statistical<K, V> {
    /**
     * Updates statistics - wraps insertion and (possibly) deletion in one operation
     *
     * @param value: the new value of the stored node
     * @param newPrimaryKey: the new primary key of the node
     * @param oldPrimaryKey: the previous primary key, as (potentially) currently stored
     * @param secondaryKey: the secondary key of the node
     * @param isDelete: if true - this is a delete operation (no re-insertion made)
     */
    fun update(value: V, newPrimaryKey: K, oldPrimaryKey: K, secondaryKey: K, isDelete: Boolean = false)

    /**
     * Collects the highest-valued elements in the statistics
     *
     * @param k: desired amount of top elements to query from the statistics
     * @return a list of the [k] highest-valued nodes' values, according to a comparator.
     *  If there are less than [k] elements stored: that many elements are returned
     */
    fun topK(k: Int = 10): List<V>
}