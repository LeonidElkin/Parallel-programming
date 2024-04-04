import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import kotlin.streams.asStream

class TestEntriesProvider : ArgumentsProvider {

    private val entries = prepareEntries()

    private fun prepareEntries(): MutableList<Arguments> {
        val arr: MutableList<Arguments> = mutableListOf()
        for (threads in intArrayOf(50)) {
            for (ops in intArrayOf(1_000_000)) {
                arr += Arguments.of(TestEntry(threads, ops, ops, 1_000))
            }
        }
        return arr
    }

    override fun provideArguments(context: ExtensionContext) = entries.asSequence().asStream()
}