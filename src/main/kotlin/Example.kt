import search.*


data class StateImpl(private val letter: Char): State {
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


fun makeGraph() = Graph(emptyMap(), {s -> StateImpl.parseString(s)})

fun makeProblem(graph: Graph): Problem {
    return try {
        Problem(graph, StateImpl('S'), StateImpl('G'))
    } catch (e: NodeNotFoundException) {
        throw RuntimeException(e.message)
    }
}


val DepthFirst = depthFirst()
//val DepthFirst = DepthFirstImpl()

val BreadthFirst = breadthFirst()

val DepthLimited = depthLimited(2)

val IterativeDeepening = iterativeDeepening()

val Uniform = object : IAlgorithm {
    override fun search(problem: Problem, printExpansion: ((List<Path>) -> Unit)?): Boolean {
        return uniform().search(problem) { fringe -> printExpansion(fringe, true) }
    }

    override fun getName() = uniform().getName()

}

val Algorithms = listOf(DepthFirst, BreadthFirst, DepthLimited, IterativeDeepening, Uniform)

