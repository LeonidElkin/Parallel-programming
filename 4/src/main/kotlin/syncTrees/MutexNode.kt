package syncTrees

import AbstractNode
import kotlinx.coroutines.sync.Mutex

class MutexNode<K : Comparable<K>, V>(
    key: K,
    value: V?,
    var mutex: Mutex = Mutex()
) : AbstractNode<K, V, MutexNode<K, V>>(key, value)