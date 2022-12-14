package year2022.day13

import com.github.h0tk3y.betterParse.combinators.or
import com.github.h0tk3y.betterParse.combinators.separatedTerms
import com.github.h0tk3y.betterParse.combinators.times
import com.github.h0tk3y.betterParse.combinators.use
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import java.io.File
import kotlin.math.max

sealed interface SignalElement : Comparable<SignalElement> {
    data class Single(val value: Int) : SignalElement {
        override fun compareTo(other: SignalElement): Int = when (other) {
            is Single -> value.compareTo(other.value)
            is Many -> Many(listOf(Single(value))).compareTo(other)
        }

        override fun toString(): String = value.toString()
    }
    data class Many(val value: List<SignalElement>) : SignalElement {
        override fun compareTo(other: SignalElement): Int = when (other) {
            is Single -> this.compareTo(Many(listOf(Single(other.value))))
            is Many -> this.value.compareToSignal(other.value)
        }

        override fun toString(): String = value.toString()
    }
}

fun List<SignalElement>.compareToSignal(other: List<SignalElement>): Int {
    for (i in (0 until max(this.size, other.size))) {
        if (i >= this.size) return -1
        if (i >= other.size) return +1
        val compareResult = this[i].compareTo(other[i])
        if (compareResult != 0) return compareResult
    }
    return 0
}

val signalsParser = object : Grammar<List<Pair<SignalElement, SignalElement>>>() {
    val intToken by regexToken("\\d+")
    val commaToken by literalToken(",")
    val openToken by literalToken("[")
    val closeToken by literalToken("]")
    val newLineToken by literalToken("\n")

    val singleElement: Parser<SignalElement.Single> by intToken use { SignalElement.Single(text.toInt()) }
    val anyElement by (singleElement or parser(this::manyElement))
    val separatedElements: Parser<List<SignalElement>> by separatedTerms(anyElement, commaToken, acceptZero = true)
    val manyElement: Parser<SignalElement.Many> by (openToken * separatedElements * closeToken) use {
        SignalElement.Many(t2)
    }

    val signalEntry by manyElement * newLineToken use { t1 }
    val signalPair by signalEntry * signalEntry use { t1 to t2 }

    override val rootParser by separatedTerms(signalPair, newLineToken)
}

fun main() {
    val input = File("./data/2022/input_13.txt").readText()
    val signals = signalsParser.parseToEnd(input)
    println(signals.withIndex().sumOf { if (it.value.first < it.value.second) it.index + 1 else 0 })
    val divider2 = SignalElement.Many(listOf(SignalElement.Many(listOf(SignalElement.Single(2)))))
    val divider6 = SignalElement.Many(listOf(SignalElement.Many(listOf(SignalElement.Single(6)))))
    val flatSignalsWithDividers = signals.flatMap { listOf(it.first, it.second) } + listOf(divider2, divider6)
    val sortedSignals = flatSignalsWithDividers.sorted()
    println((sortedSignals.indexOf(divider2) + 1) * (sortedSignals.indexOf(divider6) + 1))
}
