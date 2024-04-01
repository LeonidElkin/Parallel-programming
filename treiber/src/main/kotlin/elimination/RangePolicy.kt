package elimination

import kotlin.math.max
import kotlin.math.min

class RangePolicy(private val limit: Int) {
    private var requests: Int = 0
    private var successes: Int = 0
    private var timeouts: Int = 0
    private var range: Int = 1

    fun range(): Int {
        requests++
        if (successes + 5 >= requests / 2) return range
        range = if (timeouts - 5 >= requests / 2) max((range - 1), 1)
        else min(range + 1, limit)
        return range
    }

    fun recordEliminationSuccess() {
        successes++
        refresh()
    }

    fun recordEliminationTimeout() {
        timeouts++
        refresh()
    }

    private fun refresh() {
        if (requests < 100) return
        requests = 0
        successes = 0
        timeouts = 0
    }
}
