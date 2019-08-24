import com.authzee.kotlinguice4.getInstance
import com.google.inject.Guice
import il.ac.technion.cs.softwaredesign.database.SecureCachedStorage
import il.ac.technion.cs.softwaredesign.database.datastructures.AVLTree
import il.ac.technion.cs.softwaredesign.storage.SecureStorageFactory
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

var charset = Charsets.UTF_8

class AVLTreeTest {

    private val injector = Guice.createInjector(LibraryTestModule())
    private var storageFactory = injector.getInstance<SecureStorageFactory>()
    private var tree: AVLTree

    init {
        storageFactory = injector.getInstance()
        tree = AVLTree(SecureCachedStorage(storageFactory.open("root".toByteArray())), ::compareKeys)
    }

    @Test
    internal fun `inserting one element and reading it`() {
        tree.insert("1000000/2019".toByteArray(charset), "".toByteArray(charset))
        assertTrue("".toByteArray(charset).contentEquals(tree.search("1000000/2019".toByteArray(charset))!!))
    }

    @Test
    internal fun `inserting multiple elements and reading them`() {
        for (i in 0..100) {
            tree.insert(("100000$i/2019").toByteArray(charset), "$i".toByteArray(charset))
        }
        for (i in 0..100) {
            assertTrue("$i".toByteArray(charset).contentEquals(tree.search(("100000$i/2019").toByteArray(charset))!!))
        }
    }

    @Test
    internal fun `inserting one element and deleting it`() {
        tree.insert("1000000/2019".toByteArray(charset), "".toByteArray(charset))
        tree.delete("1000000/2019".toByteArray(charset))
    }

    @Test
    internal fun `inserting multiple elements and deleting them`() {
        val list: MutableList<Int> = (0..200).toMutableList()
        list.shuffle()
        for (i in list) {
            tree.insert(("$i/2019").toByteArray(charset), "$i".toByteArray(charset))
        }
        for (i in list.shuffled()) {
            assertTrue("$i".toByteArray(charset).contentEquals(tree.search("$i/2019".toByteArray(charset))!!))
            tree.delete("$i/2019".toByteArray(charset))
            assertEquals(null, tree.search("$i/2019".toByteArray(charset)))
        }
    }

    @Test
    internal fun `inserting and deleting one by one`() {
        val list: MutableList<Int> = (0..10000).toMutableList()
        list.shuffle()
        for (i in list) {
            tree.insert(("$i/2019").toByteArray(charset), "$i".toByteArray(charset))
            assertTrue("$i".toByteArray(charset).contentEquals(tree.search("$i/2019".toByteArray(charset))!!))
            tree.delete("$i/2019".toByteArray(charset))
            assertEquals(null, tree.search("$i/2019".toByteArray(charset)))
        }
    }

    @Test
    internal fun `top k test`() {
        for (i in 0..9) {
            tree.insert(("$i/2019").toByteArray(charset), "$i".toByteArray(charset))
        }
        for (i in 1..10) {
            for (j in 9 downTo 10 - i) {
                assertTrue(tree.topK(i).contains("$j"))
            }
            for (j in 10 - i - 1 downTo 1) {
                assertFalse(tree.topK(i).contains("$j"))
            }
        }
    }

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