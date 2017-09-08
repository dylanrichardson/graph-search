package search


data class Problem(
        val stateSpace: Graph,
        val initialNode: Node,
        val goalState: State) {

    init {
        stateSpace.getNode(initialNode.state)
    }

    constructor(stateSpace: Graph, initialState: State, goalState: State) : this(stateSpace, stateSpace.getNode(initialState), goalState)
}

data class Path(
        val nodes: List<Node>,
        val cost: Double)
    : Comparable<Path> {

    companion object {
        val comparator = compareBy<Path> { it.cost }
                .thenBy { it.end }
                .thenBy { it.length }
                .thenBy { it.nodes.joinToString() }
    }

    override fun compareTo(other: Path): Int {
        return comparator.compare(this, other)
    }

    override fun toString() = nodes.map { n -> n.state }.joinToString(separator = ",", prefix = "<", postfix = ">")

    fun nextNode() = nodes[0]

    fun didVisit(node: Node) = nodes.contains(node)

    val length = nodes.size

    fun addNode(node: Node, edgeCost: Double): Path {
        return Path(listOf(node) + nodes, edgeCost + cost)
    }

    val end = nodes.getOrNull(0)

    val heuristic = end?.heuristic ?: 0.0

    val eval = cost + heuristic
}

interface IAlgorithm {
    fun search(problem: Problem, printExpansion: ((List<Path>, (Path) -> Any) -> Unit)? = null): Boolean
    fun getName(): String
}

data class Algorithm(
        private val name: String,
        private val expansionOrder: Comparator<in Node> = reverseOrder(),
        private val depthLimit: Int? = null,
        private val widthLimit: Int? = null,
        private val addToFringe: (List<Path>, Path) -> List<Path>,
        private val prefix: ((Path) -> Any) = {""})
    : IAlgorithm {

    // General_Search
    override fun search(problem: Problem, printExpansion: ((List<Path>, (Path) -> Any) -> Unit)?): Boolean {
        // create fringe
        val root = Path(listOf(problem.initialNode), 0.0)
        val fringe = listOf(root)
        // recursively search and expand fringe
        return searchAndExpand(fringe, problem, printExpansion ?: { _, _ ->})
    }

    override fun getName() = name

    private fun searchAndExpand(fringe: List<Path>, problem: Problem, printExpansion: (List<Path>, (Path) -> Any) -> Unit): Boolean {
        // check if goal not found
        if (fringe.isEmpty())
            return false

        // print expansion step
        printExpansion(fringe, prefix)

        // get the first state and path in the fringe
        val path = fringe[0]
        val restFringe = fringe.subList(1, fringe.size)
        val nodeToExpand = path.nextNode()

        // check if state is goal
        if (nodeToExpand.state == problem.goalState)
            return true

        // expand the state, remove visited states, and sort by algorithm
        val children = problem.stateSpace
                .expandNode(nodeToExpand)
                .filterNot { path.didVisit(it) }
                .sortedWith(expansionOrder.reversed())

        // check depth limit if applicable
        val nextFringe = if (path.isAtDepthLimit()) restFringe else {
            // add a new path for each child to the fringe
            children.fold(restFringe) { newFringe, child ->
                val edgeCost = problem.stateSpace.costBetween(nodeToExpand, child) ?: 0.0
                val newPath = path.addNode(child, edgeCost)
                addToFringe(newFringe, newPath)
            }
        }

        // prune fringe if applicable and over limit
        val prunedFringe = if (nextFringe.isOverWidthLimit()) nextFringe.prune() else nextFringe

        // search next fringe
        return searchAndExpand(prunedFringe, problem, printExpansion)
    }

    private fun List<Path>.prune(): List<Path> {
        val max = sortedBy(Path::heuristic).take(widthLimit!!).last().heuristic
        return filterNot { it.heuristic > max }.take(widthLimit)
    }

    private fun List<Path>.isAtNewLevel() = distinctBy(Path::length).size == 1

    private fun List<Path>.isOverWidthLimit() = widthLimit != null && isAtNewLevel() && size > widthLimit

    private fun Path.isAtDepthLimit() = depthLimit != null && length > depthLimit
}

// SEARCHES

// helpers

fun addToFront(fringe: List<Path>, path: Path) = listOf(path) + fringe

fun addToBack(fringe: List<Path>, path: Path) = fringe + listOf(path)

fun <T : Comparable<T>> addAndSortBy(selector: (Path) -> T?) = { fringe: List<Path>, path: Path ->
    addToBack(fringe, path).sortedWith(compareBy(selector).then(Path.comparator))
}

fun Path.findAlternatePath(others: List<Path>): Path? {
    return others.firstOrNull { path -> path.end == end}
}

fun aStarAdd(fringe: List<Path>, newPath: Path): List<Path> {
    val alternatePath = newPath.findAlternatePath(fringe)
    val newFringe = ArrayList(fringe)
    if (alternatePath != null) {
        if (alternatePath.eval < newPath.eval)
            return fringe
        else
            newFringe.remove(alternatePath)
    }
    return addAndSortBy(Path::eval)(newFringe, newPath)
}

// uninformed

fun depthFirst() = Algorithm(
        name = "Depth 1st search",
        expansionOrder = naturalOrder(),
        addToFringe = ::addToFront
)

fun breadthFirst() = Algorithm(
        name = "Breadth 1st search",
        addToFringe = ::addToBack
)

fun depthLimited(depth: Int) = Algorithm(
        name = "Depth-limited search (depth-limit = 2)",
        expansionOrder = naturalOrder(),
        addToFringe = ::addToFront,
        depthLimit = depth
)

fun iterativeDeepening() = object : IAlgorithm {
    override fun getName() = "Iterative deepening search"

    override fun search(problem: Problem, printExpansion: ((List<Path>, (Path) -> Any) -> Unit)?): Boolean {
        val shouldPrint = printExpansion != null
        // repeat depth limited search with incrementing limit
        return generateSequence(0) { it + 1 }.any { limit ->
            if (shouldPrint) println("L=$limit")
            val success = depthLimited(limit).search(problem, printExpansion)
            if (shouldPrint) println()
            success
        }
    }
}

fun uniformCost() = Algorithm(
        name = "Uniform Search (Branch-and-bound)",
        addToFringe = addAndSortBy(Path::cost),
        prefix = Path::cost
)

// informed

fun greedy() = Algorithm(
        name = "Greedy search",
        addToFringe = addAndSortBy(Path::heuristic),
        prefix = Path::heuristic
)

fun aStar() = Algorithm(
        name = "A*",
        addToFringe = ::aStarAdd,
        prefix = Path::eval
)

fun beam(width: Int) = Algorithm(
        name = "Beam search (w = $width)",
        addToFringe = ::addToBack,
        widthLimit = width,
        prefix = Path::heuristic
)

fun hillClimbing() = Algorithm(
        name = "Hill climbing",
        expansionOrder = compareBy(Node::heuristic),
        addToFringe = ::addToFront,
        prefix = Path::heuristic
)

