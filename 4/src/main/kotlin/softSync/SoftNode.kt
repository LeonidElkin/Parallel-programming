package softSync

import AbstractNode
import kotlinx.coroutines.sync.Mutex

class SoftNode<K : Comparable<K>, V>(
    key: K,
    value: V?,
    var mutex: Mutex = Mutex()
) : AbstractNode<K, V, SoftNode<K, V>>(key, value)