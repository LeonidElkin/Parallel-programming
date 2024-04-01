import java.util.EmptyStackException
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.min

open class LockFreeStack<T> : Stack<T> {

    private var top = AtomicReference<Node<T>>(null)
    private var backoff: Backoff = Backoff(MIN_DELAY, MAX_DELAY)

    companion object {
        const val MIN_DELAY = 1
        const val MAX_DELAY = 2
    }

    private class Backoff(minDelay: Int, val maxDelay: Int) {
        var limit: Int

        init {
            limit = minDelay
        }

        @Throws(InterruptedException::class)
        fun backoff() {
            val delay = ThreadLocalRandom.current().nextInt(limit)
            limit = min(maxDelay.toDouble(), (2 * limit).toDouble()).toInt()
            Thread.sleep(delay.toLong())
        }
    }



    private fun tryPush(node: Node<T>): Boolean {
        val oldTop = top.get()
        node.next = oldTop
        return (top.compareAndSet(oldTop, node))
    }

    override fun push(value: T) {
        val node = Node(value)
        while (true) {
            if (tryPush(node)) {
                return
            } else {
                backoff.backoff()
            }
        }
    }

    @Throws(EmptyStackException::class)
    internal fun tryPop(): Node<T>? {
        val oldTop: Node<T> = top.get() ?: throw EmptyStackException()
        val newTop = oldTop.next
        return if (top.compareAndSet(oldTop, newTop)) oldTop else null
    }

    @Throws(EmptyStackException::class)
    override fun pop(): T {
        while (true) {
            val returnNode = tryPop()
            if (returnNode != null) {
                return returnNode.item
            } else {
                backoff.backoff()
            }
        }
    }

    override fun head(): T? = top.get()?.item
}