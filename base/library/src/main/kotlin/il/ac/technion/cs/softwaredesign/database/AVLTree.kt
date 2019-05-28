package il.ac.technion.cs.softwaredesign.database

/**
 * You can edit, run, and share this code.
 * play.kotlinlang.org
 */
import java.lang.Long as JLong
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.util.ArrayList
import il.ac.technion.cs.softwaredesign.database.mocks.SecureStorageMock
import il.ac.technion.cs.softwaredesign.storage.SecureStorage

var charset = Charsets.UTF_8
val storage: SecureStorage = SecureStorageMock()
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
              var height: Int = 0) {
    val gson = Gson()
    override fun toString(): String {
        return "key is: ${key.toString(charset)} and value is ${value.toString(charset)}"
    }

    public fun getLeft(): AVLNode? {
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
        return AVLNode(key, value, left, right, height)
    }

    public fun getRight(): AVLNode? {
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
        return AVLNode(key, value, left, right, height)
    }

    public fun setLeft(node: AVLNode?) {
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


    public fun setRight(node: AVLNode?) {
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

    public fun applyChanges() {
        var collection = ArrayList<Any?>()
        collection.add(key)
        collection.add(value)
        collection.add(left)
        collection.add(right)
        collection.add(height)
        val json = gson.toJson(collection)
        storage.write(key, json.toByteArray(charset))
    }
}


class AVLTree {
    var root: AVLNode? = null


    public fun isEmpty(): Boolean {
        return root == null
    }

    public fun insert(key: ByteArray, value: ByteArray) {
        root = insert(key, value, root)
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
            return AVLNode(key, value)
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
            return node
        }
        var result = node
        if (key.toString(charset) < result.key.toString(charset))
            result.setLeft(delete(result.getLeft(), key))
        else if (key.toString(charset) > result.key.toString(charset))
            result.setRight(delete(result.getRight(), key))
        else {
            if (result.getLeft() == null || result.getRight() == null) {
                var temp: AVLNode? = null
                if (temp == result.getLeft()) {
                    temp = result.getRight()
                } else {
                    temp = result.getLeft()
                }
                if (temp == null) {
                    temp = result
                    result = null
                } else {
                    result = temp
                }
            } else {
                var temp: AVLNode = minValueNode(result.getRight()!!)
                result.key = temp.key
                result.value = temp.value
                result.setRight(delete(result.getRight(), temp.key))
            }
        }
        if (result == null)
            return result
        result.height = max(height(result.getLeft()), height(result.getRight())) + 1
        result.applyChanges() // TODO: check if neccessary
        val balance = balanceFactor(result)
        // If this node becomes unbalanced, then there are 4 cases
        // Left Left Case
        if (balance > 1 && balanceFactor(result.getLeft()) >= 0)
            return rotateRight(result)

        // Left Right Case
        if (balance > 1 && balanceFactor(result.getLeft()) < 0) {
            result.setLeft(rotateLeft(result.getLeft()!!))
            return rotateRight(result)
        }

        // Right Right Case
        if (balance < -1 && balanceFactor(result.getRight()) <= 0)
            return rotateLeft(result)

        // Right Left Case
        if (balance < -1 && balanceFactor(result.getRight()) > 0) {
            result.setRight(rotateRight(result.getRight()!!))
            return rotateLeft(result)
        }
        result.applyChanges()
        return result
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


}