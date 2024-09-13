package hardSync

import AbstractTree
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class HardTree<K : Comparable<K>, V> : AbstractTree<K, V, HardNode<K, V>>() {

    private val mutex = Mutex()

    override suspend fun insert(key: K, value: V) {
        mutex.withLock {
            root = insertRec(root, HardNode(key, value))
        }
    }

    override suspend fun delete(key: K) {
        mutex.withLock {
            root = deleteRec(root, key)
        }
    }

    override suspend fun find(key: K): V? {
        mutex.withLock {
            return findRec(root, key)?.value
        }
    }

}