package search


data class Problem<State: IState>(
        val stateSpace: Graph<State>,
        val initialNode: Node,
        val goalState: State) {

    init {
        @Suppress("UNCHECKED_CAST")
        stateSpace.getNode(initialNode.getState() as State)
    }

    constructor(stateSpace: Graph<State>, initialState: State, goalState: State) : this(stateSpace, stateSpace.getNode(initialState), goalState)
}

data class Path<State: IState>(
        val states: List<State>,
        val cost: Double) {

    override fun toString() = states.joinToString(separator = ",", prefix = "<", postfix = ">")

    fun nextState() = states[0]

    fun didVisit(state: State) = states.contains(state)

    val length = states.size
}

interface Algorithm<State: IState> {
    fun search(problem: Problem<State>, printExpansion: ((List<Path<State>>) -> Unit)?): Boolean
    fun getName(): String
}

data class AlgorithmImpl<State: IState>(
        private val name: String,
        private val comparator: Comparator<in State> = naturalOrder(),
        private val depthLimit: Int? = null,
        private val addToFringe: (List<Path<State>>, Path<State>) -> List<Path<State>>)
    : Algorithm<State>{

    // General_Search
    override fun search(problem: Problem<State>, printExpansion: ((List<Path<State>>) -> Unit)?): Boolean {
        // create fringe
        @Suppress("UNCHECKED_CAST")
        val root = Path(listOf(problem.initialNode.getState() as State), 0.0)
        val fringe = listOf(root)
        // recursively search and expand fringe
        return searchAndExpand(fringe, problem, printExpansion ?: {})
    }

    override fun getName() = name

    private fun searchAndExpand(fringe: List<Path<State>>, problem: Problem<State>, printExpansion: (List<Path<State>>) -> Unit): Boolean {
        // check if goal not found
        if (fringe.isEmpty())
            return false

        // print expansion step
        printExpansion(fringe)

        // get the first state and path in the fringe
        val path = fringe[0]
        val restFringe = fringe.subList(1, fringe.size)
        val stateToExpand = path.nextState()

        // check if state is goal
        if (stateToExpand == problem.goalState)
            return true

        // expand the state, remove visited states, and sort by algorithm
        val children = problem.stateSpace
                .expandState(stateToExpand)
                .filterNot { path.didVisit(it) }
                .sortedWith(comparator)

        // check depth limit if applicable
        val nextFringe = if (atDepthLimit(path)) restFringe else {

            // add a new path for each child to the fringe
            children.fold(restFringe) { newFringe, child ->
                val edgeCost = problem.stateSpace.costBetween(stateToExpand, child) ?: 0.0
                val newPath = Path(listOf(child) + path.states, edgeCost)
                addToFringe(newFringe, newPath)
            }
        }

        // search next fringe
        return searchAndExpand(nextFringe, problem, printExpansion)
    }

    internal fun atDepthLimit(path: Path<State>) =
        if (depthLimit != null)
           path.length > depthLimit
        else
            false
}

// searches

fun <State: IState> addToFront() = { fringe: List<Path<State>>, path: Path<State> ->
    listOf(path) + fringe
}

fun <State: IState> addToBack() = { fringe: List<Path<State>>, path: Path<State> ->
    fringe + listOf(path)
}

fun <State: IState> depthFirst() = AlgorithmImpl<State>(
        name = "Depth 1st search",
        comparator = reverseOrder(),
        addToFringe = addToFront()
)

fun <State: IState> breadthFirst() = AlgorithmImpl<State>(
        name = "Breadth 1st search",
        comparator = naturalOrder(),
        addToFringe = addToBack()
)

fun <State: IState> depthLimited(depth: Int) = AlgorithmImpl<State>(
        name = "Depth-limited search (depth-limit = 2)",
        comparator = reverseOrder(),
        addToFringe = addToFront(),
        depthLimit = depth
)

fun <State: IState> iterativeDeepening() = object : Algorithm<State> {
    override fun getName() = "Iterative deepening search"

    override fun search(problem: Problem<State>, printExpansion: ((List<Path<State>>) -> Unit)?): Boolean {
        // empty string or null
        val emptyOr = if (printExpansion == null) "" else null

        return generateSequence(0) { it + 1 }.any { limit ->
            print(emptyOr ?: "L=$limit")
            val success = depthLimited<State>(limit).search(problem) { fringe ->
                val queueString = fringe.joinToString(" ", "[", "]")
                print(emptyOr ?: "   ${fringe[0].states[0]}      $queueString\n   ")
            }
            print(emptyOr ?: "\n")
            success
        }
    }

}

val DepthFirst = depthFirst<StateImpl>()

val BreadthFirst = breadthFirst<StateImpl>()

val DepthLimited = depthLimited<StateImpl>(2)

val IterativeDeepening = iterativeDeepening<StateImpl>()
