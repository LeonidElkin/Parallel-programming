import kotlinx.coroutines.sync.Mutex

abstract class AbstractTree<K : Comparable<K>, V, NODE_TYPE : AbstractNode<K, V, NODE_TYPE>> : Tree<K, V> {

    protected var root: NODE_TYPE? = null
    protected val mutex = Mutex()

    protected fun insertRec(recNode: NODE_TYPE?, insertedNode: NODE_TYPE): NODE_TYPE {
        recNode ?: return insertedNode

        when {
            insertedNode.key < recNode.key -> recNode.left = insertRec(recNode.left, insertedNode)
            insertedNode.key > recNode.key -> recNode.right = insertRec(recNode.right, insertedNode)
        }

        return recNode
    }

    protected fun findRec(node: NODE_TYPE?, key: K): NODE_TYPE? {
        node ?: return null

        return when {
            key == node.key -> node
            key < node.key -> findRec(node.left, key)
            else -> findRec(node.right, key)
        }
    }

    protected fun deleteRec(node: NODE_TYPE?, key: K): NODE_TYPE? {
        node ?: return null

        when {
            key < node.key -> node.left = deleteRec(node.left, key)
            key > node.key -> node.right = deleteRec(node.right, key)
            else -> {
                node.left ?: return node.right
                node.right ?: return node.left

                val minNode = findMin(node.right)
                node.key = minNode.key
                node.value = minNode.value
                node.right = deleteRec(node.right, minNode.key)
            }
        }
        return node
    }

    private fun findMin(node: NODE_TYPE?): NODE_TYPE {
        var current = node
        while (current?.left != null) {
            current = current.left
        }
        return current!!
    }

}
