package syncTrees.optimisticSync

import kotlinx.coroutines.sync.withLock
import syncTrees.MutexNode
import syncTrees.NotHardTree

class OptimisticTree<K : Comparable<K>, V> : NotHardTree<K, V>() {

    override suspend fun insert(key: K, value: V) = insertHelper(key, value, ::auxiliaryOptimisticSearch)

    override suspend fun delete(key: K) = deleteHelper(key, ::auxiliaryOptimisticSearch)

    override suspend fun find(key: K): V? = findHelper(key, ::auxiliaryOptimisticSearch)

    private suspend fun auxiliaryOptimisticSearch(key: K): Pair<MutexNode<K, V>?, MutexNode<K, V>?> {
        while (true) {
            val (currentNode, parentNode) = auxiliarySearch(key, true)
            val parentNodeMutex = parentNode?.mutex
            var isOk = false

            parentNodeMutex?.withLock { isOk = validate(currentNode, parentNode, key) } ?: return Pair(
                currentNode,
                parentNode
            )
            if (isOk) return Pair(currentNode, parentNode)
        }
    }

    private fun validate(currentNode: MutexNode<K, V>?, parent: MutexNode<K, V>?, key: K): Boolean {
        var current = root
        var currentParent: MutexNode<K, V>? = null

        while (current != null) {
            when {
                key < current.key -> {
                    currentParent = current
                    current = current.left
                }

                key > current.key -> {
                    currentParent = current
                    current = current.right
                }

                else -> break
            }
        }
        return current == currentNode && currentParent == parent
    }

}