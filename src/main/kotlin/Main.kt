import search.*
import java.io.File
import java.io.FileNotFoundException


fun main(args: Array<String>) {
    // only catch and print runtime exceptions
    val problem = printRuntimeException {
        // parse the args for the file path
        val filePath = parseArgs(args)
        // find the file with the path
        val file = findFile(filePath)
        // load the graph from the file
        val graph = makeGraph().load(file)
        // make the problem to search from S to G in the graph
        makeProblem(graph)
    }
    // perform a search on the problem with each algorithm
    problem?.let { runSearches(problem, Algorithms) }
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

internal fun runSearches(problem: Problem, algorithms: List<IAlgorithm>) {
    // run each algorithm on the problem
    algorithms.forEach { algorithm ->
        runSearch(algorithm, problem)
    }
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



