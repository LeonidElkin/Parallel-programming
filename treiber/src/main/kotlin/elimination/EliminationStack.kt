package elimination

import Stack
import Node
import java.util.*
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference

class EliminationStack<T>(private val arrCapacity: Int) : Stack<T> {
    private var top = AtomicReference<Node<T>>(null)
    private val eliminationArray = EliminationArray<T>(arrCapacity)
    private val policy = object : ThreadLocal<RangePolicy>() {
        @Synchronized
        override fun initialValue(): RangePolicy {
            return RangePolicy(arrCapacity)
        }
    }

    private fun tryPush(node: Node<T>): Boolean {
        val oldTop = top.get()
        node.next = oldTop
        return (top.compareAndSet(oldTop, node))
    }

    @Throws(TimeoutException::class)
    override fun push(value: T) {
        val rangePolicy: RangePolicy = policy.get()
        val node = Node(value)
        while (true) {
            if (tryPush(node)) {
                return
            } else try {
                val otherValue: T? = eliminationArray.visit(value, rangePolicy.range())
                if (otherValue == null) {
                    rangePolicy.recordEliminationSuccess()
                    return
                }
            } catch (ex: TimeoutException) {
                rangePolicy.recordEliminationTimeout()
            }
        }
    }

    @Throws(EmptyStackException::class)
    private fun tryPop(): Node<T>? {
        val oldTop: Node<T> = top.get() ?: throw EmptyStackException()
        val newTop = oldTop.next
        return if (top.compareAndSet(oldTop, newTop)) oldTop else null
    }

    @Throws(EmptyStackException::class, TimeoutException::class)
    override fun pop(): T {
        val rangePolicy: RangePolicy = policy.get()
        while (true) {
            val returnNode = tryPop()
            if (returnNode != null) {
                return returnNode.item
            } else try {
                val otherValue = eliminationArray.visit(null, rangePolicy.range())
                if (otherValue != null) {
                    rangePolicy.recordEliminationSuccess()
                    return otherValue
                }
            } catch (ex: TimeoutException) {
                rangePolicy.recordEliminationTimeout()
            }
        }
    }

    override fun head(): T? = top.get()?.item


}
