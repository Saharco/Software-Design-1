package il.ac.technion.cs.softwaredesign.database

/**
 * You can edit, run, and share this code.
 * play.kotlinlang.org
 */
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.util.ArrayList
import il.ac.technion.cs.softwaredesign.storage.SecureStorage

var charset = Charsets.UTF_8

public fun updateTree(storage: SecureStorage, value: String, newPrimaryKey: Int, oldPrimaryKey: Int,
                      secondaryKey: String, isDelete: Boolean = false) {
    val tree = AVLTree(storage)
    val prevKey = generateKey(oldPrimaryKey, secondaryKey)
    tree.delete(prevKey)
    if (!isDelete) {
        val newKey = generateKey(newPrimaryKey, secondaryKey)
        tree.insert(newKey, value.toByteArray(charset))
    }
}

fun treeTopK(storage: SecureStorage, k: Int = 10): List<String> {
    val tree = AVLTree(storage)
    val topK = mutableListOf<String>()
    tree.root?.topK(topK, k)
    return topK
}

private fun generateKey(primaryKey: Int, secondaryKey: String) =
        (String.format("%07d", primaryKey) + secondaryKey).toByteArray(charset)

fun max(l: Int?, r: Int?): Int {
    if (l == null) {
        if (r == null) {
            return 0
        }
        return r
    }
    if (r == null) {
        return l
    }
    return if (l > r) l else r
}

class AVLNode(var key: ByteArray, var value: ByteArray,
              var left: ByteArray? = null, var right: ByteArray? = null,
              var height: Int = 0,
              val storage: SecureStorage) {
    private val gson = Gson()
    override fun toString(): String {
        return "key is: ${key.toString(charset)} and value is ${value.toString(charset)}"
    }

    fun getLeft(): AVLNode? {
        if (left == null) {
            return null
        }
        val parser = JsonParser()
        val array = parser.parse(storage.read(left!!)!!.toString(charset)).asJsonArray
        val key = gson.fromJson(array.get(0), ByteArray::class.java)
        val value = gson.fromJson(array.get(1), ByteArray::class.java)
        val left = gson.fromJson(array.get(2), ByteArray::class.java)
        val right = gson.fromJson(array.get(3), ByteArray::class.java)
        val height = gson.fromJson(array.get(4), Int::class.java)
        return AVLNode(key, value, left, right, height, storage)
    }

    fun getRight(): AVLNode? {
        if (right == null) {
            return null
        }
        val parser = JsonParser()
        val array = parser.parse(storage.read(right!!)!!.toString(charset)).asJsonArray
        val key = gson.fromJson(array.get(0), ByteArray::class.java)
        val value = gson.fromJson(array.get(1), ByteArray::class.java)
        val left = gson.fromJson(array.get(2), ByteArray::class.java)
        val right = gson.fromJson(array.get(3), ByteArray::class.java)
        val height = gson.fromJson(array.get(4), Int::class.java)
        return AVLNode(key, value, left, right, height, storage)
    }

    fun setLeft(node: AVLNode?) {
        if (node == null) {
            left = null
            applyChanges()
            return
        }
        var collection = ArrayList<Any?>()
        collection.add(node.key)
        collection.add(node.value)
        collection.add(node.left)
        collection.add(node.right)
        collection.add(node.height)
        val json = gson.toJson(collection)
        storage.write(node.key, json.toByteArray(charset))
        left = node.key
        height = max(node.height, node.getLeft()?.height)
        applyChanges()
    }


    fun setRight(node: AVLNode?) {
        if (node == null) {
            right = null
            applyChanges()
            return
        }
        var collection = ArrayList<Any?>()
        collection.add(node.key)
        collection.add(node.value)
        collection.add(node.left)
        collection.add(node.right)
        collection.add(node.height)
        val json = gson.toJson(collection)
        storage.write(node.key, json.toByteArray(charset))
        right = node.key
        height = max(node.height, node.getLeft()?.height)
        applyChanges()
    }

    fun size(): Int {
        return 1 + (getLeft()?.size() ?: 0) + (getRight()?.size() ?: 0)
    }

    fun applyChanges() {
        var collection = ArrayList<Any?>()
        collection.add(key)
        collection.add(value)
        collection.add(left)
        collection.add(right)
        collection.add(height)
        val json = gson.toJson(collection)
        storage.write(key, json.toByteArray(charset))
    }

    fun printTree() {
        val left = getLeft()
        val right = getRight()
        println("key: " + key.toString(charset) + ", value: " + value.toString(charset))
        if (left != null) {
            left.printTree()
        }
        if (right != null) {
            right.printTree()
        }
    }

    fun topK(list: MutableList<String>, k: Int) {
        if (list.size == k) return
        getRight()?.topK(list, k)
        list.add(value.toString(charset))
        getLeft()?.topK(list, k)
    }
}


class AVLTree(val storage: SecureStorage) {

    private val gson = Gson()
    var root: AVLNode?

    init {
        val parser = JsonParser()
        val array = parser.parse(storage.read("root".toByteArray(charset))?.toString(charset)).asJsonArray
        if (array != null) {
            val key = gson.fromJson(array.get(0), ByteArray::class.java)
            val value = gson.fromJson(array.get(1), ByteArray::class.java)
            val left = gson.fromJson(array.get(2), ByteArray::class.java)
            val right = gson.fromJson(array.get(3), ByteArray::class.java)
            val height = gson.fromJson(array.get(4), Int::class.java)
            root = AVLNode(key, value, left, right, height, storage)
        } else {
            root = null
        }
    }


    public fun isEmpty(): Boolean {
        return root == null
    }

    public fun insert(key: ByteArray, value: ByteArray) {
        root = insert(key, value, root)
        val collection = ArrayList<Any?>()
        collection.add("root".toByteArray(charset))
        collection.add(root?.value)
        collection.add(root?.left)
        collection.add(root?.right)
        collection.add(root?.height)
        val json = gson.toJson(collection)
        storage.write("root".toByteArray(charset), json.toByteArray(charset))
    }

    private fun height(node: AVLNode?): Int {
        return node?.height ?: -1
    }

    override fun toString(): String {
        return "root:" + root?.key?.toString(charset) + " : " + root?.value?.toString(charset) + "\n" +
                "left:" + root?.getLeft()?.toString() + "\n" +
                "right:" + root?.getRight()?.toString() + "\n"
    }

    // returns the top node in the current stage
    private fun insert(key: ByteArray, value: ByteArray, node: AVLNode?): AVLNode {
        if (node == null) {
            return AVLNode(key, value, storage = storage)
        }
        if (key.toString(charset) < node.key.toString(charset)) {
            node.setLeft(insert(key, value, node.getLeft()))
        } else {
            node.setRight(insert(key, value, node.getRight()))
        }
        node.height = 1 + max(height(node.getLeft()), height(node.getRight()))
        node.applyChanges()

        return balance(node, key)
    }

    private fun balance(node: AVLNode, key: ByteArray): AVLNode {
        //balance
        val balance = balanceFactor(node)
        // Left Left
        if (balance > 1 && key.toString(charset) < node.getLeft()!!.key.toString(charset)) {
            return rotateRight(node)
        }
        // Right Right
        if (balance < -1 && key.toString(charset) > node.getRight()!!.key.toString(charset)) {
            return rotateLeft(node)
        }
        // Left Right
        if (balance > 1 && key.toString(charset) > node.getLeft()!!.key.toString(charset)) {
            node.setLeft(rotateLeft(node.getLeft()!!))
            return rotateRight(node)
        }
        // Right Left
        if (balance < -1 && key.toString(charset) < node.getRight()!!.key.toString(charset)) {
            node.setRight(rotateRight(node.getRight()!!))
            return rotateLeft(node)
        }
        return node
    }

    fun delete(key: ByteArray): AVLNode? {
        return delete(root, key)
    }

    private fun delete(node: AVLNode?, key: ByteArray): AVLNode? {
        if (node == null) {
            return null
        }
        if (key.toString(charset) > node.key.toString(charset)) {
            node.setRight(delete(node.getRight(), key))
            return node
        } else if (key.toString(charset) < node.key.toString(charset)) {
            node.setLeft(delete(node.getLeft(), key))
            return node
        }
        // node was found
        if (node.getLeft() == null) {
            if (node.getRight() == null) {
                return null
            }
            return node.getRight()
        }
        if (node.getRight() == null) {
            return node.getLeft()
        }
        val succ = getSucc(node)
        node.key = succ.key
        node.value = succ.value
        node.applyChanges()
        return node
    }

    //  the tree has 2 children so the function is OK
    private fun getSucc(node: AVLNode): AVLNode {
        var prev = node
        var curr = node.getRight()!!
        while (curr.getLeft() != null) {
            prev = curr
            curr = curr.getLeft()!!
        }
        if (prev.key.toString(charset) == node.key.toString(charset)) {
            prev.setRight(null)
            return curr
        }
        prev.setLeft(null)
        return curr
    }


    private fun minValueNode(node: AVLNode): AVLNode {
        var current: AVLNode? = node
        while (current!!.getLeft() != null) {
            current = current.getLeft()
        }
        return current
    }

    private fun balanceFactor(node: AVLNode?): Int {
        return height(node!!.getLeft()) - height(node.getRight())
    }

    private fun rotateRight(x: AVLNode): AVLNode {
        var y: AVLNode = x.getLeft()!!
        x.setLeft(y.getRight())
        y.setRight(x)
        x.height = 1 + max(height(x.getLeft()), height(x.getRight()))
        x.applyChanges()
        y.height = 1 + max(height(y.getLeft()), height(y.getRight()))
        y.applyChanges()
        return y
    }

    private fun rotateLeft(x: AVLNode): AVLNode {
        var y: AVLNode = x.getRight()!!
        x.setRight(y.getLeft())
        y.setLeft(x)
        x.height = 1 + max(height(x.getLeft()), height(x.getRight()))
        x.applyChanges()
        y.height = 1 + max(height(y.getLeft()), height(y.getRight()))
        y.applyChanges()
        return y
    }

    fun search(key: ByteArray): ByteArray? {
        return search(root, key)
    }

    private fun search(node: AVLNode?, key: ByteArray): ByteArray? {
        if (node == null) {
            return null
        }
        var curr = node
        var result: ByteArray? = null
        while (curr != null) {
            if (curr.key.toString(charset) > key.toString(charset)) {
                curr = curr.getLeft()
            } else if (curr.key.toString(charset) < key.toString(charset)) {
                curr = curr.getRight()
            } else {
                result = curr.value
                break
            }
        }
        return result
    }

    fun size(): Int {
        return root?.size() ?: 0
    }

    fun printTree() {
        root?.printTree()
    }

}