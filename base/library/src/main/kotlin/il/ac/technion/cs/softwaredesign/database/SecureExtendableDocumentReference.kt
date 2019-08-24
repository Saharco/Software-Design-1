package il.ac.technion.cs.softwaredesign.database

import il.ac.technion.cs.softwaredesign.storage.SecureStorage

/**
 * Implementation of [ExtendableDocumentReference].
 *
 * This class is abstract - it can only be constructed via [SecureCollectionReference]
 */
abstract class SecureExtendableDocumentReference internal constructor(path: String,
                                                                      storage: SecureStorage)
    : SecureDocumentReference(path, storage), ExtendableDocumentReference {

    override fun collection(name: String): CollectionReference {
        return object : SecureCollectionReference(path + name.replace("/", ""), storage) {}
    }
}