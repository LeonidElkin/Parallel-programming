abstract class AbstractNode<K : Comparable<K>, V, NODE_TYPE : AbstractNode<K, V, NODE_TYPE>>(
    var key: K,
    var value: V?,
    var left: NODE_TYPE? = null,
    var right: NODE_TYPE? = null
)
