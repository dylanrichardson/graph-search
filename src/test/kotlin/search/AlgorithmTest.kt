package search

import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.FreeSpec

class AlgorithmTest : FreeSpec() {
    init {
        "AlgorithmImpl" - {

            val start = StateImpl('S')
            val goal = StateImpl('G')
            val problem = Problem(Graph<StateImpl>().addEdge(start, goal, 1.0), start, goal)
            val depthLimit = 1
            val algorithm = AlgorithmImpl<StateImpl>("test", naturalOrder(), depthLimit, addToFront())

            "search" - {
                algorithm.search(problem, {}) shouldBe true
            }

            "atDepthLimit" - {
                DepthFirst.atDepthLimit(Path(emptyList(), 0.0)) shouldBe false
            }
        }

        "Problem" - {
            "constructor throws error if initial or goal state not in graph" {
                val state = StateImpl('A')
                val exception = shouldThrow<NodeNotFoundException> {
                    Problem(Graph(), state, state)
                }
                exception.message shouldBe nodeNotFoundExceptionMsg(state)
            }
        }
    }
}