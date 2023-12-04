package year2023.day4

import java.io.File

fun main() {
    val inputs = File("./data/2023/input_4.txt").readLines().map { line ->
        val split = line.drop("Card   1: ".length).split(" | ")
        Pair(
            split[0].split(" ").filterNot { it.isEmpty() }.map { it.toInt() },
            split[1].split(" ").filterNot { it.isEmpty() }.map { it.toInt() }
        )
    }
    inputs.sumOf { (w, h) ->
        val wc = h.count { it in w }
        when (wc) {
            0 -> 0
            1 -> 1
            else -> 1 shl (wc - 1)
        }
    }.also { println(it) }
    val withId = inputs.mapIndexed { i, x -> i to x }
    val winsById = mutableMapOf<Int, Int>()
    withId.reversed().sumOf { (id, card) ->
        val currentWins = card.second.count { it in card.first }
        val additionalCards = if (currentWins > 0) {
            (id + 1 .. id + currentWins).sumOf { winsById.getValue(it) }
        } else 0
        (1 + additionalCards).also { winsById[id] = it }
    }.also { println(it) }
}