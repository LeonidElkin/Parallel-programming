package correctness

import TreiberStack
import elimination.EliminationStack
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.stream.Stream

class StackProvider : ArgumentsProvider {

    override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> {
        return Stream.of(
            Arguments.of(TreiberStack<Pair<Int, Int>>()),
            Arguments.of(EliminationStack<Pair<Int, Int>>(8))
        )
    }

}