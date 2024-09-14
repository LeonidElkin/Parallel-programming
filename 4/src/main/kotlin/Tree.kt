interface Tree<K : Comparable<K>, V> {
    suspend fun insert(key: K, value: V)
    suspend fun find(key: K): V?
    suspend fun delete(key: K)
}