package correctness

import Tree
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource

class GeneralTest {

    private val threads = 8
    private val iterations = 1000


    //Checks that all the nodes are correctly inserted into the tree
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @ParameterizedTest
    @ArgumentsSource(TreeProvider::class)
    fun insertTest(tree: Tree<Int, Int>) {
        val values = Array(iterations) { i -> i }
        runBlocking {
            repeat(threads) { thread ->
                launch(newSingleThreadContext(thread.toString())) {
                    repeat(iterations) {
                        tree.insert(it, it)
                    }
                }
            }
        }
        runBlocking { for (i in values) assertEquals(i, tree.find(i)) }
    }

    //Checks that all the nodes are correctly deleted from the tree
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @ParameterizedTest
    @ArgumentsSource(TreeProvider::class)
    fun deleteAllTest(tree: Tree<Int, Int>) {
        val values = Array(iterations) { i -> i }
        runBlocking {
            repeat(threads) { thread ->
                launch(newSingleThreadContext(thread.toString())) {
                    repeat(iterations) {
                        tree.insert(it, it)
                    }
                }
            }
        }

        runBlocking {
            values.shuffle()
            repeat(threads) { thread ->
                launch(newSingleThreadContext(thread.toString())) {
                    repeat(iterations) {
                        tree.delete(it)
                    }
                }
            }

        }

        runBlocking { for (i in values) assertEquals(null, tree.find(i)) }
    }

    //Checks that some nodes are correctly deleted from the tree and some nodes are stayed inside the tree
    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    @ParameterizedTest
    @ArgumentsSource(TreeProvider::class)
    fun deleteTest(tree: Tree<Int, Int>) {
        val values = Array(iterations) { i -> i }
        val valuesNotToDelete = Array(50) { i -> iterations / (i + 2) }
        runBlocking {
            repeat(threads) { thread ->
                launch(newSingleThreadContext(thread.toString())) {
                    repeat(iterations) {
                        tree.insert(it, it)
                    }
                }
            }
        }

        runBlocking {
            values.shuffle()
            repeat(threads) { thread ->
                launch(newSingleThreadContext(thread.toString())) {
                    repeat(iterations) {
                        if (!valuesNotToDelete.contains(it)) tree.delete(it)
                    }
                }
            }

        }

        runBlocking { for (i in valuesNotToDelete) assertEquals(i, tree.find(i)) }
    }

}