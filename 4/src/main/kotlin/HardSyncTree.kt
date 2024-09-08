import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class HardSyncTree<K : Comparable<K>, V> : BSTree<K, V>() {
    private val mutex = Mutex()
    override suspend fun insert(key: K, value: V) {
        mutex.withLock {
            root = insertRec(root, key, value)
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