package il.ac.technion.cs.softwaredesign.database

import il.ac.technion.cs.softwaredesign.storage.SecureStorage

abstract class CourseAppExtendableDocument internal constructor(path: String, storage: SecureStorage) :
        CourseAppDocument(path, storage), ExtendableDocumentReference {

    override fun collection(name: String): CollectionReference {
        return object : CourseAppCollection(
                path + name.replace("/", ""), storage) {}
    }
}