import elimination.EliminationStack
import kotlinx.coroutines.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.system.measureNanoTime


class PerformanceTest {

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    @ParameterizedTest
    @ArgumentsSource(TestEntriesProvider::class)
    fun performanceTest(entry: TestEntry) {
        val timing: MutableList<Long> = mutableListOf()
        println("Default: ${entry.threads} ${entry.pushes} ${entry.pops} ${entry.heads}")

        for (st in 1..2) {

            val times: MutableList<Long> = mutableListOf()
            for (i in 1..100) {
                val stack: Stack<Int> = if (st == 1) EliminationStack(entry.threads) else TreiberStack()
                val operations = arrayOf(entry.pushes, entry.pops, entry.heads)
                val curTime = measureNanoTime {
                    var threadName = 0
                    runBlocking {
                        repeat(entry.threads) {
                            threadName += 1
                            launch(newSingleThreadContext(threadName.toString())) {
                                operation(operations, stack)
                            }
                        }
                    }
                }
                times += curTime
            }
            timing += (times.sum() / times.count())
        }

        println("Trieber Stack: ${timing[1]}, Elimination Stack: ${timing[0]}, Ratio: ${timing[1].toDouble() / timing[0]}")

    }

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    @ParameterizedTest
    @ArgumentsSource(TestEntriesProvider::class)
    fun performanceBlockyTest(entry: TestEntry) {
        val timing: MutableList<Long> = mutableListOf()
        println("Blocky: ${entry.threads} ${entry.pushes} ${entry.pops} ${entry.heads}")

        for (st in 1..2) {

            val times: MutableList<Long> = mutableListOf()
            for (i in 1..100) {
                val stack: Stack<Int>  = if (st == 1) EliminationStack(entry.threads) else TreiberStack()

                val operations = Array(entry.threads) { _ ->
                    arrayOf(
                        entry.pushes / entry.threads,
                        entry.pops / entry.threads,
                        entry.heads / entry.threads
                    )
                }

                val curTime = measureNanoTime {
                    runBlocking {
                        repeat(entry.threads) {
                            launch(newSingleThreadContext(it.toString())) {
                                operation(operations[it], stack)
                            }
                        }
                    }
                }
                times += curTime
            }
            timing += (times.sum() / times.count())
        }

        println("Trieber Stack: ${timing[1]}, Elimination Stack: ${timing[0]}, Ratio: ${timing[1].toDouble() / timing[0]}")

    }

    private fun operation(operations: Array<Int>, stack: Stack<Int>) {

        while (operations.sum() > 0) {

            val opIndex = (0 until 3).random().let {
                if (operations[it] > 0) return@let it
                if (operations[(it + 1) % 3] > 0) return@let (it + 1)
                return@let (it + 2)
            } % 3

            when (opIndex) {
                0 -> {
                    operations[0]--
                    stack.push(operations[0])
                }

                1 -> {
                    operations[1]--
                    stack.pop()
                }

                2 -> {
                    operations[2]--
                    stack.head()
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [10000, 100000, 1000000])
    fun sequentialPerformanceTest(entry: Int) {
        val times: MutableList<Long> = mutableListOf()
        println("Sequential: $entry $entry 1000")
        for (j in 1..500) {
            val operations = arrayOf(entry, entry, 1000)
            val stack = SequentialStack<Int>()
            val curTime = measureNanoTime {

                for (i in 0 until 3) {
                    while (operations[i] != 0) {
                        operations[i]--
                        when (i) {
                            0 -> stack.push(operations[0])
                            1 -> stack.pop()
                            2 -> stack.head()
                        }
                    }
                }
            }
            times += curTime
        }
        println(times.sum() / times.size)
    }

}