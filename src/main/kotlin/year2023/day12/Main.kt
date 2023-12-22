package year2023.day12

import java.io.File

fun main() {
    val input = File("./data/2023/input_12.txt").readLines().map { line ->
        val splitLine = line.split(" ")
        splitLine.first() to splitLine.last().split(",").map { it.toInt() }
    }
    input.sumOf {  (map, groups) ->
        countValidCompletions(map, groups)
    }.also { println(it) }
    input.sumOf {  (map, groups) ->
        val fullMap = (1 .. 5).joinToString("?") { map }
        val fullGroups = (1 .. 5).fold(emptyList<Int>()) { acc, _ -> acc + groups }
        countValidCompletions(fullMap, fullGroups)
    }.also { println(it) }
}

fun countValidCompletions(initial: String, expectedGroups: List<Int>): Long {
    tailrec fun recursively(remainingsMap: Map<String, Long>, remainingGroups: List<Int>): Long = if (remainingGroups.isEmpty()) {
        remainingsMap.filter { !it.key.contains('#') }.values.sum()
    } else {
        val newRemainingsMap = remainingsMap.flatMap { (remaining, count) ->
            remainingAfterFilling(remaining, remainingGroups.first()).map { it to count }
        }.groupBy{ (completion, _) ->
            completion.dropWhile { it == '.' }
        }.mapValues { (_, list) ->
            list.sumOf { it.second }
        }
        recursively(newRemainingsMap, remainingGroups.drop(1))
    }
    return recursively(mapOf(initial to 1L), expectedGroups)
}

fun Regex.findOverlapping(input: String): List<MatchResult> {
    val matches = mutableListOf<MatchResult>()
    var start = 0
    while (start < input.length) {
        val match = find(input, start)
        if (match == null) {
            return matches
        } else {
            matches.add(match)
            start = match.range.first + 1
        }
    }
    return matches
}

fun remainingAfterFilling(onto: String, groupSize: Int): List<String> {
    val reg = Regex("[#?]{$groupSize}")
    val matches = reg.findOverlapping(onto)
    val firstBroken = onto.indexOfFirst { it == '#' }.takeIf { it >= 0 }
    val validMatches = matches.filter {
        firstBroken == null || it.range.first <= firstBroken
    }.filter {
        it.range.last == onto.length - 1 || onto[it.range.last + 1] != '#'
    }
    return validMatches.map { match ->
        onto.drop(match.range.last + 2)
    }
}
