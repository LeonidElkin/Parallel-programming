package syncTrees.softSync

import syncTrees.NotHardTree

open class SoftTree<K : Comparable<K>, V> : NotHardTree<K, V>() {

    override suspend fun insert(key: K, value: V)  = insertHelper(key, value, ::auxiliarySoftSearch)

    override suspend fun find(key: K): V? = findHelper(key, ::auxiliarySoftSearch)

    override suspend fun delete(key: K) = deleteHelper(key, ::auxiliarySoftSearch)

    private suspend fun auxiliarySoftSearch(key: K) = auxiliarySearch(key, false)

}