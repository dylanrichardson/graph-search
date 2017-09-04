package search


interface IState: Comparable<IState>

data class StateImpl(private val letter: Char): IState {
    override fun compareTo(other: IState): Int {
        if (other is StateImpl)
            return letter.compareTo(other.letter)
        throw StateMismatchException(this, other)
    }

    override fun toString() = letter.toString()
}

internal fun stateMismatchExceptionMsg(first: IState, second: IState) = "$first $second"

internal class StateMismatchException(first: IState, second: IState) : Throwable(stateMismatchExceptionMsg(first, second))

internal fun stateFormatException(text: String) = "Could not parse state from text: $text"

class StateFormatException(text: String) : Exception(stateFormatException(text))

data class Node(
        private val state : IState,
        private val edges: Map<IState, Double> = emptyMap(),
        private val heuristic: Double = 0.0)
    : Comparable<Node> {


    fun getHeuristic() = heuristic

    fun getEdges() = edges


    fun getState() = state

    override fun compareTo(other: Node): Int {
        val stateCmp = state.compareTo(other.getState())
        return if (stateCmp != 0)
            stateCmp
        else
            heuristic.compareTo(other.getHeuristic())
    }

    fun updateEdges(update: (MutableMap<IState, Double>) -> Unit): Node {
        // copy old edges, update them, return a new node with new edges
        val newEdges = HashMap(edges)
        update(newEdges)
        return copy(edges = newEdges)
    }
}

internal fun nodeNotFoundExceptionMsg(state: IState) = "Could not find node with state: $state"

internal class NodeNotFoundException(state: IState) : Throwable(nodeNotFoundExceptionMsg(state))

data class Graph<State: IState>(val nodes: Map<State, Node> = emptyMap()) {

    // public

    fun addEdge(source: State, destination: State, weight: Double): Graph<State> {
        return this
                // add both states to graph
                .addState(source)
                .addState(destination)
                // connect both states with weight
                .connectBothStates(source, destination, weight)
    }

    fun updateHeuristic(state: State, heuristic: Double): Graph<State> {
        return updateNode(state) { node ->
            node.copy(heuristic = heuristic)
        }
    }

    fun costBetween(state1: State, state2: State): Double? {
        return getNode(state1).getEdges()[state2]
    }

    fun getNode(state: State): Node {
        val node = nodes[state]
        if (node != null)
            return node
        throw NodeNotFoundException(state)
    }

    fun expandState(state: State): List<State> {
        val node = getNode(state)
        @Suppress("UNCHECKED_CAST")
        return node.getEdges().keys.toList() as List<State>
    }

    fun  parseState(text: String): State {
        @Suppress("UNCHECKED_CAST")
        return try {
            StateImpl(text.single()) as State
        } catch (e: NoSuchElementException) {
            throw StateFormatException(text)
        }
    }

    // private

    internal fun addState(state: State): Graph<State> {
        return updateNodes {
            // if state is absent, add state as new node without edges
            it.putIfAbsent(state, Node(state))
        }
    }

    private fun connectSingleState(source: State, destination: State, weight: Double): Graph<State> {
        return updateNode(source) {
            it.updateEdges { newEdges ->
                // add destination and weight to list of edge pairs
                newEdges.put(destination, weight)
            }
        }
    }

    internal fun connectBothStates(source: State, destination: State, weight: Double): Graph<State> {
        return connectSingleState(source, destination, weight)
                .connectSingleState(destination, source, weight)
    }

    internal fun updateNodes(update: (MutableMap<State, Node>) -> Unit): Graph<State> {
        // copy old nodes, update them, return a new graph with new nodes
        val newNodes = HashMap(nodes)
        update(newNodes)
        return copy(nodes = newNodes)
    }

    internal fun updateNode(state: State, update: (Node) -> Node): Graph<State> {
        return try {
            updateNodes { newNodes ->
                newNodes.put(state, update(nodes[state]!!))
            }
        } catch (e: KotlinNullPointerException) {
            throw NodeNotFoundException(state)
        }
    }



}
