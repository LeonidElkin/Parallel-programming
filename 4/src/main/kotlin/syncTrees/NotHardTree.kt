package syncTrees

import AbstractTree
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KSuspendFunction1

abstract class NotHardTree<K : Comparable<K>, V> : AbstractTree<K, V, MutexNode<K, V>>() {

    protected suspend fun insertHelper(
        key: K,
        value: V,
        search: KSuspendFunction1<K, Pair<MutexNode<K, V>?, MutexNode<K, V>?>>
    ) {
        val (currentNode, parentNode) = search(key)
        if (currentNode == null) {
            if (parentNode == null) mutex.withLock { root ?: run { root = MutexNode(key, value) } }
            else {
                if (key < parentNode.key) parentNode.left = MutexNode(key, value)
                else parentNode.right = MutexNode(key, value)
            }
        }
    }

    protected suspend fun deleteHelper(key: K, search: KSuspendFunction1<K, Pair<MutexNode<K, V>?, MutexNode<K, V>?>>) {
        val (currentNode, parentNode) = search(key)
        currentNode?.mutex?.withLock {
            when {
                currentNode.left == null && currentNode.right == null -> changeNode(
                    currentNode,
                    parentNode,
                    null
                )

                currentNode.left == null || currentNode.right == null -> changeNode(
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

    protected suspend fun findHelper(key: K, search: KSuspendFunction1<K, Pair<MutexNode<K, V>?, MutexNode<K, V>?>>) =
        search(key).first?.value

    protected open suspend fun auxiliarySearch(
        key: K,
        isOptimistic: Boolean
    ): Pair<MutexNode<K, V>?, MutexNode<K, V>?> {
        root ?: return Pair(null, null)

        var current = root
        var parent: MutexNode<K, V>? = null

        while (current != null) {

            val currentMutex = current.mutex
            if (!isOptimistic) {
                currentMutex.withLock {
                    when {
                        key < current!!.key -> {
                            parent = current
                            current = current!!.left
                        }

                        key > current!!.key -> {
                            parent = current
                            current = current!!.right
                        }

                        else -> {
                            return Pair(current, parent)
                        }
                    }
                }
            } else {
                when {
                    key < current!!.key -> {
                        parent = current
                        current = current!!.left
                    }

                    key > current!!.key -> {
                        parent = current
                        current = current!!.right
                    }

                    else -> {
                        return Pair(current, parent)
                    }
                }
            }

        }

        return Pair(null, parent)
    }

    private suspend fun findMinNode(node: MutexNode<K, V>): MutexNode<K, V> {
        var current = node
        var currentMutex: Mutex
        while (current.left != null) {
            currentMutex = current.mutex
            currentMutex.withLock { current = current.left!! }
        }
        return current
    }

    private suspend fun changeNode(
        currentNode: MutexNode<K, V>,
        parentNode: MutexNode<K, V>?,
        changingNode: MutexNode<K, V>?
    ) {
        parentNode?.mutex?.withLock {
            if (parentNode.left == currentNode) parentNode.left = changingNode
            else parentNode.right = changingNode
        } ?: run { mutex.withLock { root = changingNode } }
    }
}