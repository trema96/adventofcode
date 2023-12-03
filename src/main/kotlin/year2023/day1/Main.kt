package year2023.day1

import java.io.File

fun main() {
    part1()
    part2()
}

fun part1() {
    File("./data/2023/input_1.txt").readLines().sumOf {  line ->
        val digits = line.filter { it.isDigit() }
        "${digits.first()}${digits.last()}".toInt()
    }.also { println(it) }
}

fun part2() {
    File("./data/2023/input_1.txt").readLines().sumOf {  line ->
        val first = line.firstDigitInfo()
        val last = line.lastDigitInfo()
        check(first != null && last != null)
        "${wordDigits.getValue(first.second)}${wordDigits.getValue(last.second)}".also { println(it) }.toInt()
    }.also { println(it) }
}

val wordDigits = mapOf(
    "zero" to "0",
    "one" to "1",
    "two" to "2",
    "three" to "3",
    "four" to "4",
    "five" to "5",
    "six" to "6",
    "seven" to "7",
    "eight" to "8",
    "nine" to "9",
    "0" to "0",
    "1" to "1",
    "2" to "2",
    "3" to "3",
    "4" to "4",
    "5" to "5",
    "6" to "6",
    "7" to "7",
    "8" to "8",
    "9" to "9",
)
fun String.firstDigitInfo(): Pair<Int, String>? = wordDigits.mapNotNull { (key, _) ->
    (this.indexOf(key).takeIf { it >= 0 })?.to(key)
}.minByOrNull { it.first }
fun String.lastDigitInfo(): Pair<Int, String>? = wordDigits.mapNotNull { (key, _) ->
    (this.lastIndexOf(key).takeIf { it >= 0 })?.to(key)
}.maxByOrNull { it.first }