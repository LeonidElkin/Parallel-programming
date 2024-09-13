package syncTrees.optimisticSync

import syncTrees.MutexNode
import syncTrees.NotHardTree

class OptimisticTree<K : Comparable<K>, V> : NotHardTree<K, V>() {

    override suspend fun insert(key: K, value: V)  = insertHelper(key, value, ::auxiliaryOptimisticSearch)

    override suspend fun delete(key: K) = deleteHelper(key, ::auxiliaryOptimisticSearch)

    override suspend fun find(key: K): V? = findHelper(key, ::auxiliaryOptimisticSearch)

    private suspend fun auxiliaryOptimisticSearch(key: K): Pair<MutexNode<K, V>?, MutexNode<K, V>?> {
        while (true) {
            val (currentNode, parentNode) = auxiliarySearch(key, true)
            val currentNodeMutex = currentNode?.mutex
            val parentNodeMutex = parentNode?.mutex
            currentNodeMutex?.lock()
            parentNodeMutex?.lock()
            val isOk = validate(currentNode, parentNode, key)
            parentNodeMutex?.unlock()
            currentNodeMutex?.unlock()
            if (isOk) return Pair(currentNode, parentNode)
        }
    }

    private fun validate(currentNode: MutexNode<K, V>?, parent: MutexNode<K, V>?, key: K): Boolean {
        var curent = root
        var currentParent: MutexNode<K, V>? = null

        while (curent != null) {
            when {
                key < curent.key -> {
                    currentParent = curent
                    curent = curent.left
                }

                key > curent.key -> {
                    currentParent = curent
                    curent = curent.right
                }

                else -> break
            }
        }
        return curent == currentNode && currentParent == parent
    }

}