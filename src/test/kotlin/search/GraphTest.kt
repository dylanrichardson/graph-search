package search

import io.kotlintest.matchers.*
import io.kotlintest.specs.FreeSpec
import makeGraph
import StateImpl

class GraphTest : FreeSpec() {

    private val emptyGraph = makeGraph()
    private val stateA = StateImpl('A')
    private val stateB = StateImpl('B')

    init {
        "Graph" - {
            "addEdge" - {
                "adds nodes if states are not in graph and connects them" {
                    val weight = 2.0
                    val graph = emptyGraph.addEdge(stateA, stateB, weight)
                    // check states are connected
                    graph.costBetween(stateA, stateB) shouldBe weight
                    graph.costBetween(stateB, stateA) shouldBe weight
                }

                "does not add nodes if states are in graph and connects them" {
                    val weight = 2.0
                    val graph = emptyGraph
                            .addState(stateA)
                            .addState(stateB)
                            .addEdge(stateA, stateB, weight)
                    // check states are connected
                    graph.costBetween(stateA, stateB) shouldBe weight
                    graph.costBetween(stateA, stateB) shouldBe weight
                }
            }

            "updateHeuristic" - {
                "returns a new graph with updated heuristics if the node exists" {
                    val heuristic = 3.0
                    val graph = emptyGraph
                            .addEdge(stateA, stateA, 0.0)
                            .updateHeuristic(stateA, heuristic)
                    graph.getNode(stateA).heuristic shouldBe heuristic.plusOrMinus(0.00001)
                }

                "throws error if the node does not exist" {
                    val exception = shouldThrow<NodeNotFoundException> {
                        emptyGraph.updateHeuristic(stateA, 0.0)
                    }
                    exception.message shouldBe nodeNotFoundExceptionMsg(stateA)
                }
            }

            "addState" - {
                "adds new node without edges if state is not in graph" {
                    val graph = emptyGraph.addState(stateA)
                    graph.getNode(stateA) shouldBe Node(stateA, emptyMap())
                }

                "does nothing if state is in graph" {
                    val graph = emptyGraph
                            .addEdge(stateA, stateB, 0.0)
                            .addState(stateA)
                    graph.getNode(stateA).edges.keys shouldBe setOf(stateB)
                }
            }

            "connectBothStates adds both states to the other's edges" {
                val graph = emptyGraph
                        .addEdge(stateA, stateB, 0.0)
                        .connectBothStates(stateA, stateA, 0.0)
                graph.getNode(stateA).edges.keys shouldBe setOf(stateA, stateB)
            }

            "updateNodes" - {
                "returns a new graph with mutated nodes" {
                    val graph = emptyGraph
                            .addEdge(stateA, stateA, 0.0)
                            .updateNodes {
                                it.clear()
                            }
                    graph.nodes.size shouldBe 0
                }
            }

            "updateNode" - {
                "returns a new graph with the mutated node" {
                    @Suppress("UNCHECKED_CAST")
                    val edges = emptyMap<StateImpl, Double>() as Map<State, Double>
                    val graph = emptyGraph
                            .addEdge(stateA, stateA, 0.0)
                            .updateNode(stateA) {
                                it.copy(edges = edges)
                            }
                    graph.getNode(stateA).edges shouldBe edges
                }

                "throws error if node does not exist" {
                    val exception = shouldThrow<NodeNotFoundException> {
                        emptyGraph.updateNode(stateA) { it }
                    }
                    exception.message shouldBe nodeNotFoundExceptionMsg(stateA)
                }
            }

        }

        "Node" - {
            "compareTo" - {
                "compares by heuristic if states are equal" {
                    val node1 = Node(stateA, heuristic = 1.0)
                    val node2 = Node(stateA, heuristic = 2.0)
                    node1 shouldBe lt(node2)
                }

                "compares by state if states are not equal" {
                    val node1 = Node(stateB, heuristic = 1.0)
                    val node2 = Node(stateA, heuristic = 2.0)
                    node1 shouldBe gt(node2)
                }
            }
        }

        "State" - {
            "compareTo compares by letter" {
                stateA shouldBe lt(stateB)
            }
        }
    }
}