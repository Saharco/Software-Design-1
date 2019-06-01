import com.google.gson.Gson
import com.google.gson.JsonParser
import il.ac.technion.cs.softwaredesign.storage.SecureStorage
import kotlin.math.max

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
    return tree.topKTree(k)
}

private fun generateKey(primaryKey: Int, secondaryKey: String) =
        (String.format("%07d", primaryKey) + secondaryKey).toByteArray(charset)

var charset = Charsets.UTF_8
fun compareKeys(key1: ByteArray, key2: ByteArray): Int {
    val key1String = key1.toString(charset)
    val key2String = key2.toString(charset)
    val primary1 = key1String.substring(0, 7)
    val secondary1 = key1String.substring(7)
    val primary2 = key2String.substring(0, 7)
    val secondary2 = key2String.substring(7)
    if (primary1 > primary2) {
        return 1
    } else if (primary1 < primary2) {
        return -1
    }
    if (secondary1 < secondary2)
        return 1
    else if (secondary2 < secondary1)
        return -1
    return 0
}

/**
 * AVLTree implementation.
 * @param storage - the SecureStorage instanece to store the tree
 */
class AVLTree(private val storage: SecureStorage) {
    private val gson = Gson()
    private var root: AVLNode? = null

    class AVLNode(var storage: SecureStorage,
                  var key: ByteArray, var value: ByteArray,
                  var left: ByteArray? = null, var right: ByteArray? = null,
                  var height: Int = 0) {

        private val gson = Gson()

        override fun toString(): String {
            return "key is: ${key.toString(charset)} and value is ${value.toString(charset)}"
        }

        /**
         * saves the node in the storage
         */
        fun save() {
            val collection = ArrayList<Any?>()
            collection.add(key)
            collection.add(value)
            collection.add(left)
            collection.add(right)
            collection.add(height)
            val json = gson.toJson(collection)
            storage.write(key, json.toByteArray(charset))
        }

        companion object {
            fun read(storage: SecureStorage, gson: Gson, key: ByteArray?): AVLNode? {
                if (key == null) {
                    return null
                }
                val storageResult = storage.read(key) ?: return null
                val parser = JsonParser()
                val array = parser.parse(storageResult.toString(charset)).asJsonArray
                val readKey = gson.fromJson(array.get(0), ByteArray::class.java)
                val value = gson.fromJson(array.get(1), ByteArray::class.java)
                val left = gson.fromJson(array.get(2), ByteArray::class.java)
                val right = gson.fromJson(array.get(3), ByteArray::class.java)
                val height = gson.fromJson(array.get(4), Int::class.java)
                return AVLNode(storage, readKey, value, left, right, height)
            }
        }

        fun topK(list: MutableList<String>, k: Int) {
            if (list.size == k) return
            read(storage, gson, right)?.topK(list, k)
            if (list.size == k) return
            list.add(value.toString(charset))
            if (list.size == k) return
            read(storage, gson, left)?.topK(list, k)
        }

    }

    init {
        val rootJson = storage.read("root".toByteArray(charset))
        if (rootJson == null) {
            root = null
        } else {
            root = AVLNode.read(storage, gson, rootJson)
        }
    }

    private fun height(nodeKey: ByteArray?): Int {
        if (nodeKey == null) {
            return -1
        }
        val node = AVLNode.read(storage, gson, nodeKey)
        return node?.height ?: -1
    }

    /**
     * Inserts a new key, value pair to the tree
     */
    fun insert(key: ByteArray, value: ByteArray) {
        root = AVLNode.read(storage, gson, insert(key, value, root))
        storage.write("root".toByteArray(charset), root!!.key)
    }

    /**
     * Inserts a new key,value pair to the subtree based at node.
     * @return the new root key of the subtree
     */
    fun insert(key: ByteArray, value: ByteArray, node: AVLNode?): ByteArray? {
        if (node == null) {
            val newNode = AVLNode(storage, key, value)
            newNode.save()
            return newNode.key
        }
        val cmp = compareKeys(node.key, key)
        if (cmp > 0) {
            node.left = insert(key, value, AVLNode.read(storage, gson, node.left))
            node.height = max(height(node.left) + 1, node.height)
        } else if (cmp < 0) {
            node.right = insert(key, value, AVLNode.read(storage, gson, node.right))
            node.height = max(height(node.right) + 1, node.height)
        }
        node.save()
        return balance(node, key)
    }

    /**
     * balances a subtree based in node. returns the key of the new root node
     */
    private fun balance(node: AVLNode, key: ByteArray): ByteArray? {
        //balance
        val balance = balanceFactor(node)

        // Left Left
        if (balance > 1 && compareKeys(node.left!!, key) > 0) {
            return rotateRight(node.key)
        }
        // Right Right
        if (balance < -1 && compareKeys(node.right!!, key) < 0) {
            return rotateLeft(node.key)
        }
        // Left Right
        if (balance > 1 && compareKeys(node.left!!, key) < 0) {
            node.left = rotateLeft(node.left!!)
            node.save()
            return rotateRight(node.key)
        }
        // Right Left
        if (balance < -1 && compareKeys(node.right!!, key) > 0) {
            node.right = rotateRight(node.right!!)
            node.save()
            return rotateLeft(node.key)
        }
        return node.key
    }

    private fun balanceFactor(node: AVLNode): Int {
        return height(node.left) - height(node.right)
    }

    private fun rotateRight(node: ByteArray): ByteArray {
        val x = AVLNode.read(storage, gson, node)!!
        val y: AVLNode = AVLNode.read(storage, gson, x.left!!)!!
        x.left = y.right
        y.right = x.key
        x.height = 1 + max(height(x.left), height(x.right))
        x.save()
        y.height = 1 + max(height(y.left), height(y.right))
        y.save()
        return y.key
    }

    private fun rotateLeft(node: ByteArray): ByteArray {
        val x = AVLNode.read(storage, gson, node)!!
        val y: AVLNode = AVLNode.read(storage, gson, x.right!!)!!
        x.right = y.left
        y.left = x.key
        x.height = 1 + max(height(x.left), height(x.right))
        x.save()
        y.height = 1 + max(height(y.left), height(y.right))
        y.save()
        return y.key
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
            val cmp = compareKeys(curr.key, key)
            if (cmp > 0) {
                curr = AVLNode.read(storage, gson, curr.left)
            } else if (cmp < 0) {
                curr = AVLNode.read(storage, gson, curr.right)
            } else {
                result = curr.value
                break
            }
        }
        return result
    }

    /**
     * deletes a node from the tree
     */
    fun delete(key: ByteArray) {
        root = AVLNode.read(storage, gson, delete(root, key))
    }

    /**
     * deletes a node from a subtree.
     */
    private fun delete(node: AVLNode?, key: ByteArray): ByteArray? {
        if (node == null) {
            return null
        }
        val cmp = compareKeys(node.key, key)
        if (cmp > 0) {
            node.left = delete(AVLNode.read(storage, gson, node.left), key)
            node.save()
            return node.key
        } else if (cmp < 0) {
            node.right = delete(AVLNode.read(storage, gson, node.right), key)
            node.save()
            return node.key
        } else {
            // found the node that needs to be deleted
            if (node.left == null) {
                return node.right
            } else if (node.right == null) {
                return node.left
            } else {
                // the node has 2 children
                val result = replaceWithSuccessor(node)
                return result.key
            }

        }
    }

    private fun replaceWithSuccessor(node: AVLNode): AVLNode {
        var prev = node
        var curr: AVLNode = AVLNode.read(storage, gson, node.right)!!
        while (curr.left != null) {
            prev = curr
            curr = AVLNode.read(storage, gson, curr.left)!!
        }
        if (prev.key.contentEquals(node.key)) {
            prev.key = curr.key
            prev.value = curr.value
            prev.right = AVLNode.read(storage, gson, curr.right)?.key
            prev.save()
            updateHeights(root!!, prev.key)
            return prev
        }
        prev.left = curr.right
        prev.save()
        node.key = curr.key
        node.value = curr.value
        node.save()
        updateHeights(root!!, prev.key)
        return node
    }

    private fun updateHeights(curr: AVLNode, key: ByteArray): Int {
        val cmp = compareKeys(curr.key, key)
        val rightNode = AVLNode.read(storage, gson, curr.right)
        val leftNode = AVLNode.read(storage, gson, curr.left)
        if (cmp > 0) {
            if (rightNode != null) {
                curr.height = max(rightNode.height, updateHeights(leftNode!!, key))
            } else {
                curr.height = updateHeights(leftNode!!, key) + 1
            }
            curr.save()
            return curr.height
        } else if (cmp < 0) {
            if (leftNode != null) {
                curr.height = max(leftNode.height, updateHeights(rightNode!!, key))
            } else {
                curr.height = updateHeights(rightNode!!, key) + 1
            }
            curr.save()
            return curr.height
        } else {
            return -1
        }
    }
    fun topKTree(k: Int): List<String> {
        val topKList = mutableListOf<String>()
        root?.topK(topKList, k)
        return topKList
    }
}