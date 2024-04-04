package elimination

class RangePolicy(private val arrSize: Int) {
    private var successes: Int = 0
    private var timeouts: Int = 0
    private var range: Int = arrSize

    fun recordEliminationTimeout() {
        timeouts++
        if (timeouts > 10) {
            timeouts = 0
            if (range > 1) range--
        }
    }

    fun recordEliminationSuccess() {
        successes++
        if (successes > 5) {
            successes = 0
            if (range < arrSize) range++
        }
    }

    fun range() = range
}