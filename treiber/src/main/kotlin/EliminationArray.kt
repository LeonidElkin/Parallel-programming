import java.util.concurrent.*

class EliminationArray<T>(capacity: Int) {
    private val exchanger = Array(capacity) { _ -> LockFreeExchanger<T>() }

    @Throws(TimeoutException::class)
    fun visit(value: T?, range: Int): T? {
        val slot = ThreadLocalRandom.current().nextInt(range)
        return (exchanger[slot].exchange(
            value, DURATION,
            TimeUnit.MILLISECONDS
        ))
    }

    companion object {
        private const val DURATION = 1L
    }
}