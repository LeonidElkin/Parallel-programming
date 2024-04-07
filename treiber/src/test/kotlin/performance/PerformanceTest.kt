package performance

import Stack
import TreiberStack
import elimination.EliminationStack
import kotlinx.coroutines.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureNanoTime


class PerformanceTest {

    private val testAccuracy = 500

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    @ParameterizedTest
    @ArgumentsSource(TestEntriesProvider::class)
    fun randomTest(entry: TestEntry) {
        val timing: MutableList<Long> = mutableListOf()
        println("Random: ${entry.threads} ${entry.pushes} ${entry.pops} ${entry.heads}")

        for (st in 1..2) {

            val times: MutableList<Long> = mutableListOf()
            repeat (testAccuracy) {
                val stack: Stack<Int> = if (st == 1) EliminationStack(entry.threads) else TreiberStack()
                val operations =
                    arrayOf(AtomicInteger(entry.pushes), AtomicInteger(entry.pops), AtomicInteger(entry.heads))
                val curTime = measureNanoTime {
                    runBlocking {
                        repeat(entry.threads) {
                            launch(newSingleThreadContext(it.toString())) {
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
    fun randomBlockyTest(entry: TestEntry) {
        val timing: MutableList<Long> = mutableListOf()
        println("Blocky: ${entry.threads} ${entry.pushes} ${entry.pops} ${entry.heads}")

        for (st in 1..2) {

            val times: MutableList<Long> = mutableListOf()
            repeat (testAccuracy) {
                val stack: Stack<Int> = if (st == 1) EliminationStack(entry.threads) else TreiberStack()

                val operations = Array(entry.threads) { _ ->
                    arrayOf(
                        AtomicInteger(entry.pushes / entry.threads),
                        AtomicInteger(entry.pops / entry.threads),
                        AtomicInteger(entry.heads / entry.threads)
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

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    @ParameterizedTest
    @ArgumentsSource(TestEntriesProvider::class)
    fun oneOpPerThreadTest(entry: TestEntry) {
        val timing: MutableList<Long> = mutableListOf()
        println("One per thread: ${entry.threads} ${entry.pushes} ${entry.pops} ${entry.heads}")

        for (st in 1..2) {

            val times: MutableList<Long> = mutableListOf()
            repeat (testAccuracy) {
                val stack: Stack<Int> = if (st == 1) EliminationStack(entry.threads) else TreiberStack()
                val curTime = measureNanoTime {
                    var threadName = 0
                    runBlocking {
                        repeat(entry.threads) {
                            threadName++
                            launch(newSingleThreadContext(threadName.toString())) {
                                if (it % 2 == 0) {
                                    repeat(entry.pushes / entry.threads) {
                                        stack.push(it)
                                    }
                                } else {
                                    repeat(entry.pops / entry.threads) {
                                        stack.pop()
                                    }
                                }
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
    fun chessOrderTest(entry: TestEntry) {
        val timing: MutableList<Long> = mutableListOf()
        println("Chess order: ${entry.threads} ${entry.pushes} ${entry.pops} ${entry.heads}")

        for (st in 1..2) {

            val times: MutableList<Long> = mutableListOf()
            repeat (testAccuracy) {
                val stack: Stack<Int> = if (st == 1) EliminationStack(entry.threads) else TreiberStack()
                val curTime = measureNanoTime {
                    var threadName = 0
                    runBlocking {
                        repeat(entry.threads) {
                            threadName++
                            launch(newSingleThreadContext(threadName.toString())) {
                                if (it % 2 == 0) {
                                    repeat(entry.pushes / entry.threads) {
                                        if (it % 2 == 0) stack.push(it) else stack.pop()
                                    }
                                } else {
                                    repeat(entry.pops / entry.threads) {
                                        if (it % 2 == 0) stack.pop() else stack.push(it)
                                    }
                                }
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

    private fun operation(operations: Array<AtomicInteger>, stack: Stack<Int>) {

        while (operations.sumOf { it.get() } > 0) {

            val opIndex = (0 until 3).random().let {
                if (operations[it].get() > 0) return@let it
                if (operations[(it + 1) % 3].get() > 0) return@let (it + 1)
                return@let (it + 2)
            } % 3

            when (opIndex) {
                0 -> {
                    operations[0].getAndDecrement()
                    stack.push(operations[0].get())
                }

                1 -> {
                    operations[1].getAndDecrement()
                    stack.pop()
                }

                2 -> {
                    operations[2].getAndDecrement()
                    stack.head()
                }
            }
        }
    }

}