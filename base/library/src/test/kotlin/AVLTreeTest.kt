import il.ac.technion.cs.softwaredesign.database.AVLTree
import il.ac.technion.cs.softwaredesign.database.CourseAppDatabaseFactory
import il.ac.technion.cs.softwaredesign.database.mocks.SecureStorageFactoryMock
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
var charset = Charsets.UTF_8
class AVLTreeTest {
    @Test
    internal fun `basic test` () {
        val tree = AVLTree()
        tree.insert("yuval".toByteArray(charset), "nahon".toByteArray(charset))
        println(tree)
        println(tree.root?.size())
        tree.insert("sahar".toByteArray(charset), "cohen".toByteArray(charset))
        println(tree)
        println(tree.root?.size())
        tree.insert("yuval1".toByteArray(charset), "nahon".toByteArray(charset))
        println(tree)
        println(tree.root?.size())
        tree.insert("sahar1".toByteArray(charset), "cohen".toByteArray(charset))
        println(tree)
        println(tree.root?.size())
        tree.insert("sahar2".toByteArray(charset), "cohen".toByteArray(charset))
        println(tree)
        println(tree.root?.size())
    }
    @Test
    internal fun `adding a lot of elements` () {
        val tree = AVLTree()
        for (i in 0..100) {
            tree.insert(("yuval" + i).toByteArray(charset), ("nahon" + i).toByteArray(charset))
        }
        for (i in 0..100) {
            assertEquals(tree.search("yuval$i".toByteArray(charset))?.toString(charset), "nahon$i")
        }
        assertEquals(tree.size(), 101)
        println(tree.root?.height)
        assertEquals(tree.search("yuval2".toByteArray(charset))?.toString(charset), "nahon2")
    }
    @Test
    internal fun `delete TONS of elements` () {
        val tree = AVLTree()
        for (i in 0..10) {
            tree.insert(("yuval" + i).toByteArray(charset), ("nahon" + i).toByteArray(charset))
            assertEquals(i + 1, tree.size())
        }
        for (i in 0..10) {
            assertEquals(tree.search("yuval$i".toByteArray(charset))?.toString(charset), "nahon$i")
        }
        assertEquals(tree.size(), 11)
        println(tree.root?.height)
        assertEquals(tree.search("yuval2".toByteArray(charset))?.toString(charset), "nahon2")
    }
}