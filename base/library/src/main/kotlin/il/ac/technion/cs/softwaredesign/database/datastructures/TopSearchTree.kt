package il.ac.technion.cs.softwaredesign.database.datastructures

/**
 * Represents a binary search tree (BST) with a topK query function to get the top elements
 */
interface TopSearchTree<K, V> : Dictionary<K, V> {
    /**
     * Returns the top k elements according to the tree's comparator in reverse order, such that the largest element is first in the list.
     * If the tree contains less than [k] elements: this many elements will be returned
     *
     * @param k: number of top elements to fetch from the tree
     * @return list of the [k] largest elements in the tree
     */
    fun topK(k: Int): List<Any>
}