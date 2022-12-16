package year2022.day16

import java.io.File
import java.util.LinkedList
import kotlin.system.measureTimeMillis

data class Valve(
    val name: String,
    val flowRate: Int,
    val leadsTo: Set<String>
) {
    companion object {
        fun parse(valveString: String): Valve {
            val split = valveString.split(" ")
            return Valve(
                split[1],
                split[4].drop("rate=".length).dropLast(1).toInt(),
                split.drop(9).map { it.dropLastWhile { c -> c == ',' } }.toSet()
            )
        }
    }
}

data class Path(
    val visitedValves: List<Pair<String, Int>>, // valve name to opening minute
    val totalCostToLast: Int
)

fun main() {
    val allValves = File("./data/2022/input_16.txt").readLines().map(Valve.Companion::parse)
    val valvesByName = allValves.associateBy { it.name }
    val valvesDistances: Map<String, Map<String, Int>> = allValves.map { it.name }.associateWith { start ->
        val currDistances = mutableMapOf(start to 0)
        val toExplore = LinkedList(listOf(start))
        while (toExplore.isNotEmpty()) {
            val currExploring = toExplore.removeFirst()
            valvesByName.getValue(currExploring).leadsTo.map { valvesByName.getValue(it) }.forEach { currTo ->
                if (currDistances[currTo.name] == null) {
                    currDistances[currTo.name] = currDistances.getValue(currExploring) + 1
                    toExplore.addLast(currTo.name)
                }
            }
        }
        currDistances
    }
    val valvesWithFlow = allValves.filter { it.flowRate > 0 }

    fun allPathsWithMaxCost(maxCost: Int, maximalOnly: Boolean, excludeSet: Set<String>): Sequence<Path> {
        val filteredValves = valvesWithFlow.filterNot { it.name in excludeSet }

        fun extendRecursively(
            path: Path
        ): Sequence<Path> {
            val nextCandidates = filteredValves
                .filter { it.name !in path.visitedValves.map { x -> x.first } }
                .map { nextValve ->
                    val costToReachAndOpen =
                        valvesDistances.getValue(path.visitedValves.last().first).getValue(nextValve.name) + 1
                    path.copy(
                        visitedValves = path.visitedValves + (
                                nextValve.name to path.totalCostToLast + costToReachAndOpen
                                ),
                        totalCostToLast = path.totalCostToLast + costToReachAndOpen
                    )
                }
                .filter { it.totalCostToLast < maxCost }
            return if (nextCandidates.isEmpty()) {
                sequenceOf(path)
            } else {
                val nextSequence = nextCandidates.asSequence().flatMap { extendRecursively(it) }
                if (maximalOnly) nextSequence else sequenceOf(path) + nextSequence
            }
        }

        val startPaths = filteredValves.map {
            val costToReachAndOpen =
                valvesDistances.getValue("AA").getValue(it.name) + 1
            Path(listOf(it.name to costToReachAndOpen), costToReachAndOpen)
        }
        return startPaths.asSequence().flatMap { extendRecursively(it) }
    }

    fun calcDumpedFor(
        path: Path,
        maxCost: Int
    ): Int = path.visitedValves.sumOf { (valveName, openTime) ->
        valvesByName.getValue(valveName).flowRate * (maxCost - openTime)
    }

    println(allPathsWithMaxCost(30, true, emptySet()).maxOf { calcDumpedFor(it, 30) })
    val elephantPathsByVisitedSet = allPathsWithMaxCost(26, false, emptySet())
        .groupBy { it.visitedValves.map { x -> x.first }.toSet() }
        .mapValues { it.value.maxOf { x -> calcDumpedFor(x, 26) } }
    println("Part 2 took " + measureTimeMillis {
        println(
            elephantPathsByVisitedSet.maxOf { (p1set, p1Value) ->
                p1Value + elephantPathsByVisitedSet.filter { it.key.intersect(p1set).isEmpty() }.maxOf { it.value }
            }
        )
    } / 1000.0)
}
