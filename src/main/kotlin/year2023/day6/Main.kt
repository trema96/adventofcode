package year2023.day6

import java.io.File
import kotlin.system.measureTimeMillis

fun main() {
    measureTimeMillis {
        val input = File("./data/2023/input_6.txt").readLines()
        val individualRaces = input.map {
            it.dropWhile { c -> !c.isDigit() }.split("\\s+".toRegex()).map { i -> i.toLong() }
        }.let { mappedInput ->
            mappedInput.first().zip(mappedInput.last())
        }
        individualRaces.fold(1) { acc, race ->
            acc * race.countWinsChances()
        }.also { println(it) }
        input.map {
            it.filterNot { c -> !c.isDigit() }.toLong()
        }.let {
            it.first() to it.last()
        }.countWinsChances().also { println(it) }
    }.also { println("Elapsed: $it") }
}

fun Pair<Long, Long>.countWinsChances() =
    (0..first).count { holdTime -> holdTime * (first - holdTime) > second }