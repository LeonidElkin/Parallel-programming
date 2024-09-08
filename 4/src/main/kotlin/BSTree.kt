abstract class BSTree<K : Comparable<K>, V> {

    protected var root: Node<K, V>? = null

    abstract suspend fun insert(key: K, value: V)
    abstract suspend fun delete(key: K)
    abstract suspend fun find(key: K): V?

    protected fun insertRec(node: Node<K, V>?, key: K, value: V): Node<K, V> {
        node ?: return Node(key, value)

        when {
            key < node.key -> node.left = insertRec(node.left, key, value)
            key > node.key -> node.right = insertRec(node.right, key, value)
            else -> node.value = value
        }

        return node
    }

    protected fun findRec(node: Node<K, V>?, key: K): Node<K, V>? {
        node ?: return null

        return when {
            key == node.key -> node
            key < node.key -> findRec(node.left, key)
            else -> findRec(node.right, key)
        }
    }

    protected fun deleteRec(node: Node<K, V>?, key: K): Node<K, V>? {
        node ?: return null

        when {
            key < node.key -> node.left = deleteRec(node.left, key)
            key > node.key -> node.right = deleteRec(node.right, key)
            else -> {
                node.left ?: return node.right
                node.right?: return node.left

                val minNode = findMin(node.right)
                node.key = minNode.key
                node.value = minNode.value
                node.right = deleteRec(node.right, minNode.key)
            }
        }
        return node
    }

    private fun findMin(node: Node<K, V>?): Node<K, V> {
        var current = node
        while (current?.left != null) {
            current = current.left
        }
        return current!!
    }

}
