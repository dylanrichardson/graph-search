import search.*

// everything specific to the letters example

internal data class StateImpl(private val letter: Char): State {
    override fun compareTo(other: State): Int {
        if (other is StateImpl)
            return letter.compareTo(other.letter)
        throw StateMismatchException(this, other)
    }

    override fun toString() = letter.toString()

    companion object {
        fun parseString(string: String): State {
            return try {
                StateImpl(string.single())
            } catch (e: NoSuchElementException) {
                throw StateFormatException(string)
            }
        }
    }
}


internal fun makeGraph() = Graph(emptyMap(), {s -> StateImpl.parseString(s)})

internal fun makeProblem(graph: Graph): Problem {
    return try {
        Problem(graph, StateImpl('S'), StateImpl('G'))
    } catch (e: NodeNotFoundException) {
        throw RuntimeException(e.message)
    }
}

internal fun runSearch(algorithm: IAlgorithm, problem: Problem) {
    println("${algorithm.getName()}\n")
    println("   Expanded  Queue")
    val success = algorithm.search(problem, ::printExpansion)
    if (success) {
        println("      goal reached!")
    }
    println()
    println()
}

internal fun printExpansion(fringe: List<Path>, prefix: (Path) -> Any = { _ -> "" }) {
    val queueString = fringe.joinToString(separator = " ", prefix = "[", postfix = "]") { path ->
        prefix(path).toString() + path.toString()
    }
    println("      ${fringe[0].nodes[0].state}      $queueString")
}

val Algorithms = listOf(
        depthFirst(),
        breadthFirst(),
        depthLimited(2),
        iterativeDeepening(),
        uniformCost(),
        greedy(),
        aStar(),
        beam(2),
        hillClimbing()
)

