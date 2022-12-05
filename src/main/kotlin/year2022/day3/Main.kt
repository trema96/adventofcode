package year2022.day3

import java.io.File

fun main() {
    part1()
    part2()
}

fun part2() {
    File("./data/2022/input_3.txt").readLines().withIndex().groupBy { it.index / 3 }.toList().sumOf { (_, v) ->
        val intersect = v.map { it.value.toSet() }.reduce { a, b -> a.intersect(b) }
        require(intersect.size == 1)
        charToPoint(intersect.first())
    }.also { println(it) }
}

fun part1() {
    File("./data/2022/input_3.txt").readLines().sumOf { line ->
        val off = line.take(line.length / 2).toSet().intersect(line.drop(line.length / 2).toSet())
        check(off.size == 1)
        charToPoint(off.first())
    }.also { println(it) }
}

val charToPointMap = (('a' .. 'z') + ('A' .. 'Z')).mapIndexed { i, c -> c to (i + 1) }.toMap()

fun charToPoint(c: Char): Int = charToPointMap.getValue(c)