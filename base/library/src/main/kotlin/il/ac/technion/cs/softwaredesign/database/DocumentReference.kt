package il.ac.technion.cs.softwaredesign.database


/**
 *  Reference to a document in the database which stores information in a field-value fashion.
 *
 *  Akin to *files* in the file system
 */
interface DocumentReference {

    /**
     * Sets a field in the document.
     * The information will be stored upon creating the document with a [write]
     *
     * @param field: 1st value: field's name. 2nd value: data
     */
    fun set(field: Pair<String, String>): DocumentReference

    /**
     * Sets a field in the document that corresponds to a list.
     * The information will be stored upon creating the document with a [write]
     *
     * @param field: field's name
     * @param list: list of strings to store in this field
     */
    fun set(field: String, list: List<String>): DocumentReference

    /**
     * Sets multiple fields in the document.
     * The information will be stored upon creating the document with a [write]
     *
     * @param data: map of field-value information
     */
    fun set(data: Map<String, String>): DocumentReference

    /**
     * Write the document to the database.
     *
     * This is a *terminal* operation
     */
    fun write()

    /**
     * Read a document's field from the database.
     *
     * This is a *terminal* operation
     *
     * @param field: name of the field from which the desired information will be read
     */
    fun read(field: String): String?


    fun readList(field: String): List<String>?

    /**
     * Update information of a document in the database. This operation may be performed on an existing document
     *
     * This is a *terminal* operation
     */
    fun update()

    /**
     * Delete a document from the database.
     *
     * This is a *terminal* operation
     */
    fun delete()

    /**
     * Delete a document's fields from the database.
     *
     * This is a *terminal* operation
     *
     * @param fields: list of fields to be deleted
     */
    fun delete(fields: List<String>)

    /**
     * Returns whether or not the document exists in the database.
     *
     * This is a *terminal* operation
     */
    fun exists(): Boolean
}