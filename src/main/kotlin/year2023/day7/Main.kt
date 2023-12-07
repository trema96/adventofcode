package year2023.day7

import java.io.File

fun main() {
    val handsToBid = File("./data/2023/input_7.txt").readLines().map {
        val split = it.split(" ")
        Hand(split[0]) to split[1].toLong()
    }
    println(handsToBid.sortedWith(DumbComparator).mapIndexed { index, (_, bid) -> (index + 1) * bid }.sum())
    println(handsToBid.sortedWith(DumbJokerComparator).mapIndexed { index, (_, bid) -> (index + 1) * bid }.sum())
}

fun Char.toCardValue() = when (this) {
    'A' -> 14
    'K' -> 13
    'Q' -> 12
    'J' -> 11
    'T' -> 10
    else -> checkNotNull(this.digitToIntOrNull()?.takeIf { it in 2..9 }) { "Invalid card $this" }
}

fun Char.toJokerCardValue() = when (this) {
    'A' -> 14
    'K' -> 13
    'Q' -> 12
    'J' -> 1
    'T' -> 10
    else -> checkNotNull(this.digitToIntOrNull()?.takeIf { it in 2..9 }) { "Invalid card $this" }
}

class Hand(
    val cards: String
) {
    private val cardsCount =
        cards.toList().also { require(it.size == 5) }.groupingBy { it }.eachCount()

    val typeRank: Int = when (cardsCount.size) {
        1 -> 7
        2 -> when (cardsCount.values.toSet()) {
            setOf(1, 4) -> 6
            setOf(2, 3) -> 5
            else -> throw IllegalStateException(cardsCount.toString())
        }
        3 -> when (cardsCount.values.toSet()) {
            setOf(3, 1, 1) -> 4
            setOf(2, 2, 1) -> 3
            else -> throw IllegalStateException(cardsCount.toString())
        }
        4 -> when (cardsCount.values.toSet()) {
            setOf(2, 1, 1, 1) -> 2
            else -> throw IllegalStateException(cardsCount.toString())
        }
        5 -> 1
        else -> throw IllegalStateException(cardsCount.toString())
    }

    val jokerTypeRank by lazy {
        val nonJokerReplacement = cardsCount.filterKeys { it != 'J' }.maxByOrNull { it.value }
        if (nonJokerReplacement != null)
            maxOf(this.typeRank, Hand(cards.replace('J', nonJokerReplacement.key)).typeRank)
        else
            this.typeRank
    }

//    Poker style comparison :/
//
//    private val cardsCount =
//        cards.map { it.toCardValue() }.also { require(it.size == 5) }.groupingBy { it }.eachCount()
//
//    private val sortedCardTypesByCountValue =
//        cardsCount.toList().sortedByDescending { it.first }.sortedByDescending { it.second }.map { it.first }
//
//    override fun compareTo(other: Hand): Int = when {
//        this.typeRank == other.typeRank -> compareCards(other)
//        this.typeRank > other.typeRank -> 1
//        else -> -1
//    }
//
//    private fun compareCards(other: Hand): Int = when {
//        this.sortedCardTypesByCountValue == other.sortedCardTypesByCountValue -> 0
//        this.indexOfFirstBetterCardThan(other) < other.indexOfFirstBetterCardThan(this) -> 1
//        else -> -1
//    }
//
//    private fun indexOfFirstBetterCardThan(other: Hand) =
//        this.sortedCardTypesByCountValue.zip(other.sortedCardTypesByCountValue).indexed().firstOrNull {
//            it.second.first > it.second.second
//        }?.first ?: Int.MAX_VALUE

    override fun toString(): String =
        cards
}

fun compareIndividualCards(a: Hand, b: Hand, cardValue: Char.() -> Int) =
    a.cards.zip(b.cards).firstNotNullOfOrNull { (ca, cb) ->
        when {
            ca.cardValue() > cb.cardValue() -> 1
            cb.cardValue() > ca.cardValue() -> -1
            else -> null
        }
    } ?: 0

object DumbComparator : Comparator<Pair<Hand, Long>> {
    override fun compare(a: Pair<Hand, Long>, b: Pair<Hand, Long>): Int =
        compare(a.first, b.first)

    private fun compare(a: Hand, b: Hand): Int = when {
        a.typeRank == b.typeRank -> compareIndividualCards(a, b) { toCardValue() }
        a.typeRank > b.typeRank -> 1
        else -> -1
    }
}

object DumbJokerComparator : Comparator<Pair<Hand, Long>> {
    override fun compare(a: Pair<Hand, Long>, b: Pair<Hand, Long>): Int =
        compare(a.first, b.first)

    private fun compare(a: Hand, b: Hand): Int = when {
        a.jokerTypeRank == b.jokerTypeRank -> compareIndividualCards(a, b) { toJokerCardValue() }
        a.jokerTypeRank > b.jokerTypeRank -> 1
        else -> -1
    }
}