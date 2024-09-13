package syncTrees.hardSync

import AbstractNode

class HardNode<K : Comparable<K>, V>(
    key: K,
    value: V?
) : AbstractNode<K, V, HardNode<K, V>>(key, value)