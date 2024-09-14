package correctness

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import syncTrees.hardSync.HardTree
import syncTrees.optimisticSync.OptimisticTree
import syncTrees.softSync.SoftTree
import java.util.stream.Stream

class TreeProvider : ArgumentsProvider {
    override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of(HardTree<Int, Int>()),
            Arguments.of(OptimisticTree<Int, Int>()),
            Arguments.of(SoftTree<Int, Int>())
        )
    }
}