package elimination

import java.util.concurrent.*
import kotlin.time.DurationUnit

class EliminationArray<T>(capacity: Int) {
    private val exchanger = Array(capacity) { _ -> Exchanger<T>() }

    @Throws(TimeoutException::class)
    fun visit(value: T?, range: Int): T? {
        val slot = ThreadLocalRandom.current().nextInt(range)
        return (exchanger[slot].exchange(
            value, DURATION,
            DurationUnit.MILLISECONDS
        ))
    }

    companion object {
        private const val DURATION = 1L
    }
}