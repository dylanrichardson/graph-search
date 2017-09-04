package search

import java.io.File

// public

fun <State: IState> Graph<State>.load(file: File): Graph<State> {
    // read file as list of string
    val lines = file.readLines()
    // separate file into edges and heuristics
    val (edges, heuristics) = separateLines(lines)
    // add to graph
    return addEdges(edges).addHeuristics(heuristics)
}

// private

internal val Separator = "#####"

internal fun separateLines(lines: List<String>): Pair<List<String>, List<String>> {
    val before = lines.takeWhile { it != Separator }
    val after = lines.drop(before.count() + 1)
    return Pair(before, after)
}

internal fun <State: IState> Graph<State>.addEdges(lines: List<String>): Graph<State> {
    // parse each line updating the graph
    return lines.fold(this) { newGraph, line ->
        val (source, destination, weight) = parseEdge(line)
        newGraph.addEdge(source, destination, weight)
    }
}

internal fun <State: IState> Graph<State>.parseEdge(line: String): Triple<State, State, Double> {
    // parse for format "<source> <destination> <weight>"
    val tokens = line.split(" ")
    return handleLineParseException(line, ::edgeFormatExceptionMsg) {
        Triple(parseState(tokens[0]), parseState(tokens[1]), tokens[2].toDouble())
    }
}

internal fun <State: IState> Graph<State>.addHeuristics(lines: List<String>): Graph<State> {
    // parse each line adding the heuristic to the graph
    return lines.fold(this) { newGraph, line ->
        try {
            val (state, weight) = parseHeuristic(line)
            newGraph.updateHeuristic(state, weight)
        } catch (e: NodeNotFoundException) {
            throw RuntimeException(heuristicFormatExceptionMsg(line))
        }
    }
}

internal fun <State: IState> Graph<State>.parseHeuristic(line: String): Pair<State, Double> {
    // parse for format "<state> <heuristic>"
    val tokens = line.split(" ")
    return handleLineParseException(line, ::heuristicFormatExceptionMsg) {
        Pair(parseState(tokens[0]), tokens[1].toDouble())
    }
}

internal fun edgeFormatExceptionMsg(line: String) = "Could not parse edge: $line"

internal fun heuristicFormatExceptionMsg(line: String) = "Could not parse heuristic: $line"

internal fun <T> handleLineParseException(line: String, toMsg: (String) -> String, block: () -> T): T {
    return try {
        block()
    } catch (e: Exception) {
        throw when (e){
            is IndexOutOfBoundsException,
            is NumberFormatException,
            is NoSuchElementException,
            is IllegalArgumentException,
            is StateFormatException-> RuntimeException(toMsg(line))
            else -> e
        }
    }
}
