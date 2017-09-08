package search

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.FreeSpec
import makeGraph
import StateImpl
import findFile

class AlgorithmTest : FreeSpec() {
    init {
        "Algorithm" - {

            val start = StateImpl('S')
            val goal = StateImpl('G')
            val problem = Problem(makeGraph().addEdge(start, goal, 1.0), start, goal)
            val depthLimit = 1
            val algorithm = Algorithm("test", naturalOrder(), depthLimit, addToFringe = ::addToFront)

            "search" - {
                algorithm.search(problem) shouldBe true
            }
        }

//        "Uniform" - {
//            uniformCost().search(Problem(makeGraph().load(findFile("graph.txt")), StateImpl('S'), StateImpl('G'))) shouldBe true
//        }

        "Problem" - {
            "constructor throws error if initial or goal state not in graph" {
                val state = StateImpl('A')
                val exception = shouldThrow<NodeNotFoundException> {
                    Problem(makeGraph(), state, state)
                }
                exception.message shouldBe nodeNotFoundExceptionMsg(state)
            }
        }
    }
}