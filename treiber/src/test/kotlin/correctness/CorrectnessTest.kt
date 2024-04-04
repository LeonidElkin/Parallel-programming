package correctness

import Stack
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.concurrent.atomic.AtomicInteger

class CorrectnessTest {
    private val threads = 8
    private val iterations = 100000

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @ParameterizedTest
    @ArgumentsSource(StackProvider::class)
    fun pushPopDifferentJobsTest(stack: Stack<Pair<Int, Int>>) {
        val values = Array(threads) { _ -> Array(iterations) { _ -> false } }

        runBlocking {
            runBlocking {
                repeat(threads) { thread ->
                    launch(newSingleThreadContext(thread.toString())) {
                        repeat(iterations) {
                            stack.push(Pair(thread, it))
                        }
                    }
                }
            }

            runBlocking {
                repeat(threads) { thread ->
                    launch(newSingleThreadContext(thread.toString())) {
                        repeat(iterations) {
                            stack.pop()?.let { pair ->
                                assert(!values[pair.first][pair.second])
                                values[pair.first][pair.second] = true
                            }
                        }
                    }
                }
            }
        }
        assertEquals(null, stack.pop())
        for (i in values) for (j in i) assert(j)
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @ParameterizedTest
    @ArgumentsSource(StackProvider::class)
    fun pushPopOneThreadTest(stack: Stack<Pair<Int, Int>>) {
        val values = Array(threads) { _ -> Array(iterations) { _ -> false } }
        val pops = AtomicInteger(0)

        runBlocking {
            repeat(threads) { thread ->
                launch(newSingleThreadContext(thread.toString())) {
                    repeat(iterations) {
                        stack.push(Pair(thread, it))
                        stack.pop()?.let { pair ->
                            assert(!values[pair.first][pair.second])
                            values[pair.first][pair.second] = true
                            pops.getAndIncrement()
                        }
                    }
                }
            }
        }
        assertEquals(iterations * threads, pops.get())
        assertEquals(null, stack.pop())
        for (i in values) for (j in i) assert(j)
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @ParameterizedTest
    @ArgumentsSource(StackProvider::class)
    fun pushesPopsTest(stack: Stack<Pair<Int, Int>>) {
        val values = Array(threads) { _ -> Array(iterations) { _ -> false } }
        val pops = AtomicInteger(0)

        runBlocking {
            repeat(threads) { thread ->
                launch(newSingleThreadContext(thread.toString())) {
                    repeat(iterations) {
                        stack.push(Pair(thread, it))
                    }
                }
                launch(newSingleThreadContext((thread + threads).toString())) {
                    repeat(iterations) {
                        stack.pop()?.let { pair ->
                            assert(!values[pair.first][pair.second])
                            values[pair.first][pair.second] = true
                            pops.getAndIncrement()
                        }
                    }
                }
            }
        }
        while (pops.get() < iterations * threads) {
            stack.pop()?.let { pair ->
                assert(!values[pair.first][pair.second])
                values[pair.first][pair.second] = true
                pops.getAndIncrement()
            }
        }
        assertEquals(pops.get(), iterations * threads)
        assertEquals(null, stack.pop())
        for (i in values) for (j in i) assert(j)
    }
}