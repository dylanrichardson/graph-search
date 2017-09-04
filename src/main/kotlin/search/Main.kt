package search

import java.io.File
import java.io.FileNotFoundException

// public

fun main(args: Array<String>) {
    // list each algorithm to use
    val algorithms = listOf(DepthFirst, BreadthFirst, DepthLimited, IterativeDeepening)
    // only catch and print runtime exceptions
    val problem = printRuntimeException {
        // parse the args for the file path
        val filePath = parseArgs(args)
        // find the file with the path
        val file = findFile(filePath)
        // load the graph from the file
        val graph = Graph<StateImpl>().load(file)
        // make the problem to search from S to G in the graph
        makeProblem(graph, StateImpl('S'), StateImpl('G'))
    }
    // perform a search on the file with each algorithm
    problem?.let { runSearches(problem, algorithms) }
}

fun makeProblem(graph: Graph<StateImpl>, start: StateImpl, goal: StateImpl): Problem<StateImpl> {
    return try {
        Problem(graph, start, goal)
    } catch (e: NodeNotFoundException) {
        throw RuntimeException(e.message)
    }
}

// private

internal fun <T> printRuntimeException(block: () -> T): T? {
    // run block and return result
    // print runtime exceptions to std err and return null
    return try {
        block()
    } catch (e: RuntimeException) {
        System.err.println(e.message)
        null
    }
}

internal fun runSearches(problem: Problem<StateImpl>, algorithms: List<Algorithm<StateImpl>>) {
    // run each algorithm on the problem
    algorithms.forEach { algorithm ->
        runSearch(algorithm, problem)
    }
}

internal fun runSearch(algorithm: Algorithm<StateImpl>, problem: Problem<StateImpl>) {
    println("${algorithm.getName()}\n")
    println("   Expanded  Queue")
    if (algorithm.search(problem, ::printExpansion)) {
        println("      goal reached!")
    }
    println()
    println()
}

internal fun printExpansion(fringe: List<Path<StateImpl>>) {
    val queueString = fringe.joinToString(separator = " ", prefix = "[", postfix = "]")
    println("      ${fringe[0].states[0]}      $queueString")
}

internal fun fileNotFoundMsg(path: String) = "Could not find file with path: $path"

internal val FileArgExceptionMsg = "Expecting path to graph file as only argument"

internal fun findFile(path: String): File {
    return try {
        File(ClassLoader.getSystemResource(path).file)
    } catch (e: Exception) {
        // only catch file not found and NPE
        when (e) {
            is FileNotFoundException,
            is NullPointerException -> throw RuntimeException(fileNotFoundMsg(path))
            else -> throw e
        }
    }
}

internal fun parseArgs(args: Array<String>): String {
    return try {
        args[1]
    } catch (e: IndexOutOfBoundsException) {
        throw RuntimeException(FileArgExceptionMsg)
    }
}



