package softSync

import AbstractTree
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SoftTree<K : Comparable<K>, V> : AbstractTree<K, V, SoftNode<K, V>>() {

    private val mutex = Mutex()

    override suspend fun insert(key: K, value: V) {
        val (currentNode, parentNode) = auxiliarySearch(key)
        if (currentNode == null) {
            if (parentNode == null) mutex.withLock { root ?: run { root = SoftNode(key, value) } }
            else {
                if (key < parentNode.key) parentNode.left = SoftNode(key, value)
                else parentNode.right = SoftNode(key, value)
            }
        } else currentNode.mutex.unlock()
    }

    override suspend fun delete(key: K) {
        val (currentNode, parentNode) = auxiliarySearch(key)
        currentNode?.mutex?.withLock {
            when {
                currentNode.left == null && currentNode.right == null -> deleteHelper(
                    currentNode,
                    parentNode,
                    null
                )

                currentNode.left == null || currentNode.right == null -> deleteHelper(
                    currentNode,
                    parentNode,
                    currentNode.left ?: currentNode.right
                )

                else -> {
                    val minNode = findMinNode(currentNode.right!!)
                    val newKey = minNode.key
                    val newValue = minNode.value
                    delete(newKey)
                    currentNode.key = newKey
                    currentNode.value = newValue
                }
            }

        }
    }

    override suspend fun find(key: K): V? = auxiliarySearch(key).first?.value

    private suspend fun findMinNode(node: SoftNode<K, V>): SoftNode<K, V> {
        var current = node
        var currentMutex: Mutex
        while (current.left != null) {
            currentMutex = current.mutex
            currentMutex.withLock { current = current.left!! }
        }
        return current
    }

    private suspend fun deleteHelper(
        currentNode: SoftNode<K, V>,
        parentNode: SoftNode<K, V>?,
        changingNode: SoftNode<K, V>?
    ) {
        parentNode?.mutex?.withLock {
            if (parentNode.left == currentNode) parentNode.left = changingNode
            else parentNode.right = changingNode
        } ?: run { mutex.withLock { root = changingNode } }
    }

    private suspend fun auxiliarySearch(key: K): Pair<SoftNode<K, V>?, SoftNode<K, V>?> {
        root ?: return Pair(null, null)

        var current = root
        var parent: SoftNode<K, V>? = null

        while (current != null) {
            val currentMutex = current.mutex
            currentMutex.lock()
            when {
                key < current.key -> {
                    parent = current
                    current = current.left
                }

                key > current.key -> {
                    parent = current
                    current = current.right
                }

                else -> {
                    return Pair(current, parent)
                }
            }
            currentMutex.unlock()
        }

        return Pair(null, parent)
    }


}