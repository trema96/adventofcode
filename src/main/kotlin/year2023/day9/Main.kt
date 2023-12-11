package year2023.day9

import java.io.File


fun List<Long>.extrapolateNext(): Long = if (all { it == 0L}) {
    0
} else {
    last() + zipWithNext().map { (a, b) -> b - a }.extrapolateNext()
}

fun List<Long>.extrapolatePrev(): Long = if (all { it == 0L}) {
    0
} else {
    first() - zipWithNext().map { (a, b) -> b - a }.extrapolatePrev()
}

fun main() {
    val input = File("./data/2023/input_9.txt").readLines().map { l -> l.split(" ").map { it.toLong() } }
    input.sumOf { it.extrapolateNext() }.also { println(it) }
    input.sumOf { it.extrapolatePrev() }.also { println(it) }
}