package year2023.day2

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import java.io.File

data class Game(
    val id: Int,
    val rounds: List<Round>
)

data class Round(
    val extractions: List<Extraction>
)

data class Extraction(
    val count: Int,
    val color: Color
)

enum class Color {
    RED,
    GREEN,
    BLUE
}

val gamesGrammar = object : Grammar<List<Game>>() {
    val whitespaceToken by regexToken("\\s+", ignore = true)
    val colonToken by literalToken(":")
    val semicolonToken by literalToken(";")
    val gameToken by literalToken("Game")
    val intToken by regexToken("\\d+") // Can't convert now or token won't be picked up
    val commaToken by literalToken(",")
    val blueToken by literalToken("blue")
    val redToken by literalToken("red")
    val greenToken by literalToken("green")

    val intNumber by intToken use { text.toInt() }
    val color by (redToken or blueToken or greenToken) use { Color.valueOf(text.uppercase()) }

    val extraction by (intNumber * color) use { Extraction(t1, t2) }
    val extractions by separated(extraction, commaToken, false) use { terms }

    val rounds by separated(extractions, semicolonToken, false) use { terms.map { Round(it) } }

    val game by (gameToken * intNumber * colonToken * rounds) use { Game(t2, t4) }

    override val rootParser by oneOrMore(game)
}

fun main() {
    val input = File("./data/2023/input_2.txt").readText()
    val games = gamesGrammar.parseToEnd(input)
    games.filter { game ->
        game.rounds.all { round ->
            round.extractions.all { extraction ->
                extraction.count <= when (extraction.color) {
                    Color.RED -> 12
                    Color.GREEN -> 13
                    Color.BLUE -> 14
                }
            }
        }
    }.sumOf { it.id }.also { println(it) }
    games.sumOf {  game ->
        game.rounds.flatMap { it.extractions }.groupingBy { it.color }.fold(0) { acc, extraction ->
            maxOf(acc, extraction.count)
        }.values.reduce { acc, curr -> acc * curr }
    }.also { println(it) }
}