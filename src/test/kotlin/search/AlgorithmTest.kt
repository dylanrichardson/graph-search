package search

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.FreeSpec
import makeGraph
import StateImpl

class AlgorithmTest : FreeSpec() {
    init {
        "Algorithm" - {

            val start = StateImpl('S')
            val goal = StateImpl('G')
            val problem = Problem(makeGraph().addEdge(start, goal, 1.0), start, goal)
            val depthLimit = 1
            val algorithm = Algorithm("test", naturalOrder(), depthLimit, addToFront())

            "search" - {
                algorithm.search(problem, {}) shouldBe true
            }

            "atDepthLimit" - {
                algorithm.atDepthLimit(Path(emptyList(), 0.0)) shouldBe false
            }
        }

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