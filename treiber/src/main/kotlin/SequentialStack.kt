

class SequentialStack<T> : Stack<T> {
    private var top: Node<T>?  = null

    override fun push(value: T) {
        val newNode = Node(value)
        newNode.next = top
        top = newNode
    }

    override fun pop(): T? {
        val oldTop = top
        top = top?.next
        return oldTop?.item
    }

    override fun head(): T? {
        return top?.item
    }
}