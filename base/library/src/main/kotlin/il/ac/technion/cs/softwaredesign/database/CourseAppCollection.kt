package il.ac.technion.cs.softwaredesign.database

import il.ac.technion.cs.softwaredesign.storage.SecureStorage

abstract class CourseAppCollection internal constructor(path: String, val storage: SecureStorage)
    : CollectionReference {
    private val path: String = "$path/"

    override fun document(name: String): ExtendableDocumentReference {
        val hasher = Hasher()
        return object : CourseAppExtendableDocument(
                path + hasher(name), storage) {}
    }

    /*
    override fun collection(name: String): CollectionReference {
        return object : CourseAppCollection(
                path + name.replace("/", ""), storage) {}
    }

     */
}