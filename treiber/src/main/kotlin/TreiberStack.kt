import java.util.concurrent.atomic.AtomicReference
import net.jcip.annotations.*

@ThreadSafe
class TreiberStack<T> : Stack<T> {
    var top: AtomicReference<Node<T>?> = AtomicReference()

    override fun push(value: T) {
        val newHead = Node(value)
        var oldHead: Node<T>?

        do {
            oldHead = top.get()
            newHead.next = oldHead
        } while (!top.compareAndSet(oldHead, newHead))
    }

    override fun pop(): T? {
        var oldHead: Node<T>?
        var newHead: Node<T>?

        do {
            oldHead = top.get()
            if (oldHead == null) return null
            newHead = oldHead.next
        } while (!top.compareAndSet(oldHead, newHead))

        return oldHead!!.item
    }

    override fun head(): T? = top.get()?.item

}