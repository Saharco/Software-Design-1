package il.ac.technion.cs.softwaredesign.database

import il.ac.technion.cs.softwaredesign.storage.SecureStorage


/**
 * Implementation of [DocumentReference].
 *
 * This class is abstract - it can only be constructed via access from the database's root
 */
abstract class CourseAppDocument internal constructor(path: String, val storage: SecureStorage)
    : DocumentReference {
    protected val path: String = "$path/"
    private var data: HashMap<String, String> = HashMap()

    override fun set(field: Pair<String, String>): CourseAppDocument {
        data[field.first] = field.second
        return this
    }

    override fun set(data: Map<String, String>): CourseAppDocument {
        for (entry in data.entries)
            this.data[entry.key] = entry.value
        return this
    }

    /**
     * @inheritDoc
     *
     * @throws IllegalStateException if the document to be written contains no information
     * @throws IllegalArgumentException if the document to be written already exists
     */
    override fun write() {
        if (data.isEmpty())
            throw IllegalStateException("Can\'t write empty document")
        if (exists())
            throw IllegalArgumentException("Document already exists")
        allocatePath()

        for (entry in data.entries) {
            writeEntry("$path${entry.key}/", entry.value)
        }
    }

    /**
     * @inheritDoc
     *
     * Returns null if the data does not exist in the document
     */
    override fun read(field: String): String? {
        if (!isValidPath())
            return null

        val key = ("$path$field/").toByteArray()

        val value = storage.read(key)?.toList()
        if (value == null || value[0] == 0.toByte())
            return null

        return String(value
                .takeLast(value.size - 1)
                .toByteArray())
    }

    /**
     * @inheritDoc
     *
     * @throws IllegalStateException if the document to be updated contains no extra information
     */
    override fun update() {
        if (data.isEmpty())
            throw IllegalStateException("Can\'t write empty document")
        allocatePath()

        for (entry in data.entries) {
            writeEntry("$path${entry.key}/", entry.value)
        }
    }

    override fun delete() {
        deleteEntry(path)
    }

    override fun delete(fields: List<String>) {
        for (field in fields) {
            deleteEntry("$path$field/")
        }
    }

    override fun exists(): Boolean {
        return pathExists(path)
    }

    /**
     * Checks whether the document's path is valid or not.
     *
     * Path is "valid" if all the documents in the path leading to the last document exist and weren't logically written off
     */
    private fun isValidPath(): Boolean {
        val reg = Regex("(?<=/)")
        val pathSequence = ArrayList<String>(path.split(reg))
        var currentPath = pathSequence.removeAt(0)
        while (pathSequence.size > 2) {
            val extension = pathSequence.removeAt(0) + pathSequence.removeAt(0)
            currentPath += extension
            if (!pathExists(currentPath))
                return false
        }
        return true
    }

    /**
     * Checks if the desired path exists in the file system
     */
    private fun pathExists(pathToCheck: String): Boolean {
        val key = pathToCheck.toByteArray()
        val value = storage.read(key) ?: return false
        return value[0] != 0.toByte()
    }

    /**
     * Creates a prefix ByteArray block to be chained to data in order to logically turn it on/off
     */
    private fun statusBlock(activated: Boolean = true): ByteArray {
        val status = if (activated) 1 else 0
        return ByteArray(1) { status.toByte() }
    }

    /**
     * Creates all the documents in the full path leading to the final document in the path
     */


    //users/sahar/pictures/one.jpg/
    private fun allocatePath() {
        val reg = Regex("(?<=/)")
        val pathSequence = ArrayList<String>(path.split(reg))
        var currentPath = pathSequence.removeAt(0)
        while (pathSequence.size > 2) {
            val extension = pathSequence.removeAt(0) + pathSequence.removeAt(0)
            currentPath += extension
            val key = currentPath.toByteArray()
            storage.write(key, statusBlock(activated = true))
        }
    }

    /**
     * Performs a low-level write with some key (field) and its value.
     * Chains a prefix activation block to logically turn it on
     */
    private fun writeEntry(field: String, value: String) {
        val key = field.toByteArray()
        val data = statusBlock(activated = true) + value.toByteArray()
        storage.write(key, data)
    }

    /**
     * Performs a low-level delete of some key (path).
     * Chains a prefix activation block to logically turn it off
     */
    private fun deleteEntry(path: String) {
        val key = path.toByteArray()
        val store = statusBlock(activated = false)
        storage.write(key, store)
    }

}
