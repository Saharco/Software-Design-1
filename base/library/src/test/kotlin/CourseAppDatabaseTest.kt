import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import il.ac.technion.cs.softwaredesign.database.CourseAppDatabaseFactory
import il.ac.technion.cs.softwaredesign.database.mocks.SecureStorageFactoryMock
import java.lang.IllegalArgumentException

class CourseAppDatabaseTest {
    private var storageFactory = SecureStorageFactoryMock()
    private val dbFactory = CourseAppDatabaseFactory(storageFactory)


    @BeforeEach
    internal fun resetDatabase() {
        storageFactory = SecureStorageFactoryMock()
    }

    @Test
    internal fun `single field write in a document is properly read after document is written`() {
        dbFactory.open("root")
                .collection("programming languages")
                .document("kotlin")
                .set(Pair("isCool", "true"))
                .write()

        val result = dbFactory.open("root")
                .collection("programming languages")
                .document("kotlin")
                .read("isCool")

        assertEquals(result, "true")
    }

    @Test
    internal fun `multiple fields write in a document are properly read after document is written`() {
        val data = hashMapOf("date" to "April 21, 2019",
                "isColored" to "true",
                "isPublic" to "false",
                "takenAt" to "technion")

        val documentRef = dbFactory.open("root")
                .collection("users")
                .document("sahar cohen")
                .collection("photos")
                .document("awkward photo")

        documentRef.set(data)
                .write()

        assertEquals(documentRef.read("date"), "April 21, 2019")
        assertEquals(documentRef.read("isColored"), "true")
        assertEquals(documentRef.read("isPublic"), "false")
        assertEquals(documentRef.read("takenAt"), "technion")
    }

    @Test
    internal fun `reading fields or documents that do not exist should return null`() {
        dbFactory.open("root")
                .collection("users")
                .document("sahar")
                .set(Pair("eye color", "green"))
                .write()

        var result = dbFactory.open("root")
                .collection("users")
                .document("sahar")
                .read("hair color")

        assertNull(result)

        result = dbFactory.open("root")
                .collection("users")
                .document("yuval")
                .read("hair color")

        assertNull(result)
    }

    @Test
    internal fun `writing to a document that already exists should throw IllegalArgumentException`() {
        val db = dbFactory.open("root")

        db.collection("users")
                .document("sahar")
                .set(Pair("eye color", "green"))
                .write()

        assertThrows<IllegalArgumentException> {
            db.collection("users")
                    .document("sahar")
                    .set(Pair("surname", "cohen"))
                    .write()
        }
    }

    @Test
    internal fun `writing an empty document should throw IllegalStateException`() {
        assertThrows<IllegalStateException> {
            dbFactory.open("root")
                    .collection("users")
                    .document("sahar")
                    .write()
        }
    }

    @Test
    internal fun `reading document after deletion should return null`() {
        val userRef = dbFactory.open("root")
                .collection("users")
                .document("sahar")

        userRef.set(Pair("eye color", "green"))
                .write()

        userRef.delete()

        val result = userRef.read("eye color")
        assertNull(result)

    }

    @Test
    internal fun `deleting some fields in a document should not delete the others`() {
        val data = hashMapOf("date" to "April 21, 2019",
                "isColored" to "true",
                "isPublic" to "false",
                "takenAt" to "technion")

        val documentRef = dbFactory.open("root")
                .collection("users")
                .document("sahar cohen")
                .collection("photos")
                .document("awkward photo")

        documentRef.set(data)
                .write()

        documentRef.delete(listOf("isColored", "isPublic"))

        assertNull(documentRef.read("isColored"))
        assertEquals(documentRef.read("date"), "April 21, 2019")
        assertEquals(documentRef.read("takenAt"), "technion")
    }

    @Test
    internal fun `can check if a document exists`() {
        var documentRef = dbFactory.open("root")
                .collection("users")
                .document("sahar")

        documentRef.set(Pair("eye color", "green"))
                .write()

        assertTrue(documentRef.exists())

        documentRef = dbFactory.open("root")
                .collection("students")
                .document("sahar")

        assertFalse(documentRef.exists())
    }

    @Test
    internal fun `can update existing and non existing fields in a document which may or may not exist`() {
        var documentRef = dbFactory.open("root")
                .collection("users")
                .document("sahar")

        documentRef.set(Pair("favorite food", "pizza"))
                .write()

        documentRef.set(Pair("favorite food", "ice cream"))
                .set(Pair("favorite animal", "dog"))
                .update()

        assertEquals(documentRef.read("favorite food"), "ice cream")
        assertEquals(documentRef.read("favorite animal"), "dog")

        documentRef = dbFactory.open("root")
                .collection("users")
                .document("yuval")

        documentRef.set(Pair("favorite food", "pizza"))
                .update()

        assertEquals(documentRef.read("favorite food"), "pizza")
    }

    @Test
    internal fun `writing document in one database does not affect another database`() {
        val db1 = dbFactory.open("users")
        val db2 = dbFactory.open("items")

        db1.collection("root")
                .document("sahar")
                .set(Pair("age", "21"))
                .write()

        val result = db2.collection("root")
                .document("sahar")
                .read("age")

        assertNotEquals("21", result)
    }

    @Test
    internal fun `can write and read lists as a document's field`() {
        val documentRef = dbFactory.open("users")
                .collection("root")
                .document("sahar")

        val list = mutableListOf("ice cream", "pizza", "popcorn")

        documentRef.set("favorite foods", list)
                .write()

        val returnedList = documentRef.readCollection("favorite foods")

        assertTrue(returnedList!!.containsAll(list))
        assertTrue(list.containsAll(returnedList))
    }

    @Test
    internal fun `any character can be used as a document's name`() {
        val chars = generateCharactersList()
        val collectionRef = dbFactory.open("root")
                .collection("root")
        val data = Pair("key", "value")
        for (str in chars) {
            collectionRef.document(str)
                    .set(data)
                    .write()
        }
    }

    private fun generateCharactersList(): ArrayList<String> {
        val list = ArrayList<String>()
        for (i in 0 until 128) {
            list.add((i.toChar().toString()))
        }
        return list
    }
}


