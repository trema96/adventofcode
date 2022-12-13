package year2022.day11

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import java.io.File

enum class Operator(val compute: (Long, Long) -> Long) {
    SUM({ a, b -> a + b }),
    TIMES({ a, b -> a * b })
}

sealed interface Operand {
    fun getValue(oldValue: Long): Long

    data class Literal(val value: Long): Operand {
        override fun getValue(oldValue: Long): Long = this.value
    }

    object Old : Operand {
        override fun getValue(oldValue: Long): Long = oldValue

        override fun toString(): String = "Old"
    }
}

data class Operation(val left: Operand, val operator: Operator, val right: Operand) {
    fun compute(oldValue: Long) = operator.compute(left.getValue(oldValue), right.getValue(oldValue))
}

data class Monkey(
    val id: Int,
    val items: MutableList<Long>,
    val operation: Operation,
    val divisibleNumberTest: Long,
    val successTestNext: Int,
    val failureTestNext: Int
) {
    var inspectionCount: Long = 0
}

val barrelOfMonkeyGrammar = object : Grammar<List<Monkey>>() {
    val whitespaceToken by regexToken("\\s+", ignore = true)
    val colonToken by literalToken(":")
    val monkeyToken by literalToken("Monkey")
    val intToken by regexToken("\\d+") // Can't convert now or token won't be picked up
    val commaToken by literalToken(",")
    val startingItemsToken by literalToken("Starting items:")
    val operationToken by literalToken("Operation: new =")
    val testToken by literalToken("Test: divisible by")
    val testTrueToken by literalToken("If true: throw to monkey")
    val testFalseToken by literalToken("If false: throw to monkey")
    val sumToken by literalToken("+")
    val timesToken by literalToken("*")
    val oldToken by literalToken("old")

    val intNumber by intToken use { text.toInt() }

    val startingItems by (startingItemsToken * separatedTerms(intNumber, commaToken)) use { t2 }

    val operand by (oldToken asJust Operand.Old) or (intNumber map { Operand.Literal(it.toLong()) })
    val operator by (sumToken asJust Operator.SUM) or (timesToken asJust Operator.TIMES)
    val operation by (operationToken * operand * operator * operand) use { Operation(t2, t3, t4) }

    val test by (testToken * intNumber) use { t2 }
    val testTrue by (testTrueToken * intNumber) use { t2 }
    val testFalse by (testFalseToken * intNumber) use { t2 }

    val monkeyHeader by (monkeyToken * intNumber * colonToken) use { t2 }

    val monkey by (monkeyHeader * startingItems * operation * test * testTrue * testFalse) use {
        Monkey(t1, t2.map { it.toLong() }.toMutableList(), t3, t4.toLong(), t5, t6)
    }

    override val rootParser by oneOrMore(monkey)
}

fun doRound(
    monkeys: List<Monkey>,
    inspectionCommonMultiple: Long,
    applyReduction: Boolean,
    applySimplification: Boolean
) {
    monkeys.forEach { monkey ->
        monkey.items.forEach { item ->
            val newWorryInitial =
                monkey.operation.compute(item)
            val newWorryReduced =
                if (applyReduction) newWorryInitial / 3 else newWorryInitial
            val newWorrySimplified =
                if (applySimplification) newWorryReduced % inspectionCommonMultiple else newWorryReduced
            if (newWorrySimplified % monkey.divisibleNumberTest == 0L) {
                monkeys[monkey.successTestNext].items.add(newWorrySimplified)
            } else {
                monkeys[monkey.failureTestNext].items.add(newWorrySimplified)
            }
            monkey.inspectionCount += 1
        }
        monkey.items.clear()
    }
}

fun main() {
    val input = File("./data/2022/input_11.txt").readText()
    val monkeys1 = barrelOfMonkeyGrammar.parseToEnd(input)
    val monkeysInspections = monkeys1.map { it.divisibleNumberTest }
    val inspectionLCM = (1L until Long.MAX_VALUE).first { lcmCandidate ->
        monkeysInspections.all { lcmCandidate % it == 0L }
    }

    repeat(20) { doRound(monkeys1, inspectionLCM, true, false) }
    println(monkeys1.map { it.inspectionCount }.sortedDescending().take(2).reduce { a, b -> a * b})

    val monkeys2 = barrelOfMonkeyGrammar.parseToEnd(input)
    repeat(10000) { doRound(monkeys2, inspectionLCM, false, true) }
    println(monkeys2.map { it.inspectionCount }.sortedDescending().take(2).reduce { a, b -> a * b})
}
