package search

import findFile
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.FreeSpec
import makeGraph
import StateImpl

class LoadTest : FreeSpec() {

    private val emptyGraph = makeGraph()
    private val stateA = StateImpl('A')
    private val stateB = StateImpl('B')

    init {
        "loadGraph" - {
            "parses correctly formatted file into graph" {
                val graph = emptyGraph
                        .addEdge(stateA, stateB, 1.0)
                        .updateHeuristic(stateA, 2.0)
                val file = findFile("graph1.txt")
                emptyGraph.load(file) shouldBe graph
            }

            "throws error if file is incorrectly formatted" {
                val exception = shouldThrow<RuntimeException> {
                    val file = findFile("badGraph.txt")
                    emptyGraph.load(file)
                }
                exception.message shouldBe edgeFormatExceptionMsg("A B")
            }
        }

        "separateLines returns two lists before and after separator" {
            val (before, after) = separateLines(listOf("A", Separator, "B"))
            before shouldBe listOf("A")
            after shouldBe listOf("B")
        }

        "addEdges" - {
            "adds correctly formatted edges to graph" {
                val graph = emptyGraph
                        .addEdge(stateA, stateB, 1.0)
                        .addEdge(stateB, StateImpl('C'), 2.0)
                emptyGraph.addEdges(listOf("A B 1", "B C 2")) shouldBe graph
            }

            "throws error if edge is incorrectly formatted" {
                val line = "A B"
                val exception = shouldThrow<RuntimeException> {
                    emptyGraph.addEdges(listOf(line))
                }
                exception.message shouldBe edgeFormatExceptionMsg(line)
            }
        }

        "addHeuristics" - {
            "adds correctly formatted heuristics to nodes in graph" {
                val graph = emptyGraph
                        .addEdge(stateA, stateB, 1.0)
                val graphWithHeuristics = graph
                        .updateHeuristic(stateA, 1.0)
                        .updateHeuristic(stateB, 2.0)
                graph.addHeuristics(listOf("A 1", "B 2")) shouldBe graphWithHeuristics
            }

            "throws error if heuristic is incorrectly formatted" {
                val line = "A"
                val exception = shouldThrow<RuntimeException> {
                    emptyGraph.addHeuristics(listOf(line))
                }
                exception.message shouldBe heuristicFormatExceptionMsg(line)
            }
        }

        "parseEdge" - {

            fun assertEdgeFormatExceptionFor(line: String) {
                val exception = shouldThrow<RuntimeException> {
                    emptyGraph.parseEdge(line)
                }
                exception.message shouldBe edgeFormatExceptionMsg(line)
            }

            "returns the two states and cost of a correctly formatted edge" {
                val edge = Triple(stateA, stateB, 1.0)
                emptyGraph.parseEdge("A B 1") shouldBe edge
            }

            "throws error if edge has extra space between tokens" {
                assertEdgeFormatExceptionFor("A  B 1")
            }

            "throws error if edge is missing destination state" {
                assertEdgeFormatExceptionFor("A 1")
            }

            "parseEdge throws error if edge is missing weight" {
                assertEdgeFormatExceptionFor("A B")
            }

            "throws error if edge has non-char state" {
                assertEdgeFormatExceptionFor("Aa B 1")
            }
        }

        "parseHeuristic" - {

            fun assertHeuristicFormatExceptionFor(line: String) {
                val exception = shouldThrow<RuntimeException> {
                    emptyGraph.parseHeuristic(line)
                }
                exception.message shouldBe heuristicFormatExceptionMsg(line)
            }
            "returns the state and cost of a correctly formatted heuristic" {
                emptyGraph.parseHeuristic("A 1") shouldBe Pair(stateA, 1.0)
            }

            "throws error if heuristic has extra space between tokens" {
                assertHeuristicFormatExceptionFor("A  1")
            }

            "throws error if heuristic is missing state" {
                assertHeuristicFormatExceptionFor("1")
            }

            "throws error if heuristic is missing weight" {
                assertHeuristicFormatExceptionFor("A")
            }

            "throws error if heuristic has non-char state" {
                assertHeuristicFormatExceptionFor("Aa 1")
            }
        }
    }
}