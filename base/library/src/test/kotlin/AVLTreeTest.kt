
import il.ac.technion.cs.softwaredesign.mocks.SecureStorageMock
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

var charset = Charsets.UTF_8

class AVLTreeTest {

    @Test
    internal fun `inserting one element and reading it`() {
        val storage = SecureStorageMock()
        val tree = AVLTree(storage)
        tree.insert("1000000-2019".toByteArray(charset), "nahon".toByteArray(charset))
        assertTrue("nahon".toByteArray(charset).contentEquals(tree.search("1000000-2019".toByteArray(charset))!!))
    }
    @Test
    internal fun `inserting multiple elements and reading them`() {
        val storage = SecureStorageMock()
        val tree = AVLTree(storage)
        for  (i in 0..100) {
            tree.insert(("100000$i-2019").toByteArray(charset), "nahon$i".toByteArray(charset))
        }
        for (i in 0..100) {
            assertTrue("nahon$i".toByteArray(charset).contentEquals(tree.search(("100000$i-2019").toByteArray(charset))!!))
        }
    }
    @Test
    internal fun `inserting one element and deleting it`() {
        val storage = SecureStorageMock()
        val tree = AVLTree(storage)
        tree.insert("1000000-2019".toByteArray(charset), "nahon".toByteArray(charset))
        tree.delete("1000000-2019".toByteArray(charset))
    }
    @Test
    internal fun `inserting multiple elements and deleting them`() {
        val storage = SecureStorageMock()
        val tree = AVLTree(storage)
        val list : MutableList<Int> = (0..100).toMutableList()
        list.shuffle()
        for  (i in list) {
            tree.insert(("100000$i-2019").toByteArray(charset), "nahon$i".toByteArray(charset))
        }
        for (i in list.reversed()) {
            assertTrue("nahon$i".toByteArray(charset).contentEquals(tree.search("100000$i-2019".toByteArray(charset))!!))
            tree.delete("100000$i-2019".toByteArray(charset))
            assertEquals(null, tree.search("100000$i-2019".toByteArray(charset)))
        }
    }
    @Test
    internal fun `inserting and deleting one by one`() {
        val storage = SecureStorageMock()
        val tree = AVLTree(storage)
        val list : MutableList<Int> = (0..100).toMutableList()
        list.shuffle()
        for  (i in list) {
            tree.insert(("100000$i-2019").toByteArray(charset), "nahon$i".toByteArray(charset))
            assertTrue("nahon$i".toByteArray(charset).contentEquals(tree.search("100000$i-2019".toByteArray(charset))!!))
            tree.delete("100000$i-2019".toByteArray(charset))
            assertEquals(null ,tree.search("100000$i-2019".toByteArray(charset)))
        }
    }

}