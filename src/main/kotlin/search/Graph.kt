package search


interface State : Comparable<State>

internal fun stateMismatchExceptionMsg(first: State, second: State) = "$first $second"

internal class StateMismatchException(first: State, second: State) : Throwable(stateMismatchExceptionMsg(first, second))

internal fun stateFormatException(text: String) = "Could not parse state from text: $text"

class StateFormatException(text: String) : Exception(stateFormatException(text))

data class Node(
        val state : State,
        val edges: Map<State, Double> = emptyMap(),
        val heuristic: Double = 0.0)
    : Comparable<Node> {

    override fun compareTo(other: Node): Int {
        val stateCmp = state.compareTo(other.state)
        return if (stateCmp != 0)
            stateCmp
        else
            heuristic.compareTo(other.heuristic)
    }

    fun updateEdges(update: (MutableMap<State, Double>) -> Unit): Node {
        // copy old edges, update them, return a new node with new edges
        val newEdges = HashMap(edges)
        update(newEdges)
        return copy(edges = newEdges)
    }
}

internal fun nodeNotFoundExceptionMsg(state: State) = "Could not find node with state: $state"

internal class NodeNotFoundException(state: State) : Throwable(nodeNotFoundExceptionMsg(state))

data class Graph(val nodes: Map<State, Node> = emptyMap(), val parseState: (String) -> State) {

    // public

    fun addEdge(source: State, destination: State, weight: Double): Graph {
        return this
                // add both states to graph
                .addState(source)
                .addState(destination)
                // connect both states with weight
                .connectBothStates(source, destination, weight)
    }

    fun updateHeuristic(state: State, heuristic: Double): Graph {
        return updateNode(state) { node ->
            node.copy(heuristic = heuristic)
        }
    }

    fun costBetween(node1: Node, node2: Node): Double? {
        return node1.edges[node2.state]
    }

    fun getNode(state: State): Node {
        val node = nodes[state]
        if (node != null)
            return node
        throw NodeNotFoundException(state)
    }

    fun expandNode(node: Node) = node.edges.keys.toList().map { state -> getNode(state) }

    // private

    internal fun addState(state: State): Graph {
        return updateNodes {
            // if state is absent, add state as new node without edges
            it.putIfAbsent(state, Node(state))
        }
    }

    private fun connectSingleState(source: State, destination: State, weight: Double): Graph {
        return updateNode(source) {
            it.updateEdges { newEdges ->
                // add destination and weight to list of edge pairs
                newEdges.put(destination, weight)
            }
        }
    }

    internal fun connectBothStates(source: State, destination: State, weight: Double): Graph {
        return connectSingleState(source, destination, weight)
                .connectSingleState(destination, source, weight)
    }

    internal fun updateNodes(update: (MutableMap<State, Node>) -> Unit): Graph {
        // copy old nodes, update them, return a new graph with new nodes
        val newNodes = HashMap(nodes)
        update(newNodes)
        return copy(nodes = newNodes)
    }

    internal fun updateNode(state: State, update: (Node) -> Node): Graph {
        return try {
            updateNodes { newNodes ->
                newNodes.put(state, update(nodes[state]!!))
            }
        } catch (e: KotlinNullPointerException) {
            throw NodeNotFoundException(state)
        }
    }



}
