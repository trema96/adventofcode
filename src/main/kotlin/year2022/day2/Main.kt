package year2022.day2

import java.io.File
import java.lang.IllegalArgumentException

enum class Symbol(val points: Int) {
    ROCK(1),
    PAPER(2),
    SCISSORS(3);

    companion object {
        fun parse(string: String): Symbol = when (string) {
            "A", "X" -> ROCK
            "B", "Y" -> PAPER
            "C", "Z" -> SCISSORS
            else -> throw IllegalArgumentException(string)
        }
    }
}

class Match(val self: Symbol, val other: Symbol) {
    companion object {
        fun parse(string: String): Match {
            val split = string.split(" ")
            require(split.size == 2)
            return Match(self = Symbol.parse(split[1]), other = Symbol.parse(split[0]))
        }

        fun parse2(string: String): Match {
            val split = string.split(" ")
            require(split.size == 2)
            val other = Symbol.parse(split[0])
            return when (split[1]) {
                "Y" -> Match(other, other)
                "X" -> Symbol.values().map { Match(it, other) }.first { it.winner == other }
                "Z" -> Symbol.values().map { Match(it, other) }.first { it.winner != other && it.winner != null }
                else -> throw IllegalArgumentException("Invalid value")
            }
        }
    }

    val winner: Symbol? = when (setOf(self, other)) {
        setOf(Symbol.SCISSORS, Symbol.ROCK) -> Symbol.ROCK
        setOf(Symbol.SCISSORS, Symbol.PAPER) -> Symbol.SCISSORS
        setOf(Symbol.ROCK, Symbol.PAPER) -> Symbol.PAPER
        else -> null
    }

    fun pointsFor(player: Symbol): Int = when (winner) {
        null -> player.points + 3
        player -> player.points + 6
        other -> player.points
        else -> throw IllegalArgumentException("Unexpected")
    }
}

fun main() {
    val input = File("./data/2022/input_2.txt").readLines()
    println(input.map { Match.parse(it) }.sumOf { it.pointsFor(it.self) })
    println(input.map { Match.parse2(it) }.sumOf { it.pointsFor(it.self) })
}
