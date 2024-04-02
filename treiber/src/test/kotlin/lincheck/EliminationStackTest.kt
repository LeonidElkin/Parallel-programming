package lincheck

import elimination.EliminationStack
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.junit.jupiter.api.Test

class EliminationStackTest {
    private val stack = EliminationStack<Int>(100)

    @Operation
    fun pop() = stack.pop()

    @Operation
    fun head() = stack.head()

    @Operation
    fun push(value: Int) = stack.push(value)

    @Test
    fun stressTest() = StressOptions()
        .actorsBefore(2)
        .threads(2)
        .actorsPerThread(2)
        .actorsAfter(1)
        .iterations(100)
        .invocationsPerIteration(1000)
        .check(this::class)

    @Test
    fun modelCheckingTest() = ModelCheckingOptions()
        .hangingDetectionThreshold(10_000)
        .actorsBefore(2)
        .threads(2)
        .actorsPerThread(2)
        .actorsAfter(1)
        .iterations(100)
        .invocationsPerIteration(1000)
        .check(this::class)
}