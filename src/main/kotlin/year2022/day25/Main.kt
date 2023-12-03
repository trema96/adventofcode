import java.io.File
import java.lang.IllegalStateException

fun main() {
    println(decToSnafu(File("./data/2022/input_25.txt").readLines().sumOf { snafuToDec(it) }))
    println(File("./data/2022/input_25.txt").readLines().reduce(::sumSnafu))
}

val pows = sequence {
    var curr = 1L
    do {
       yield(curr)
       curr *= 5
    } while (curr * 5 > curr)
}.toList()

fun sumSnafu(a: String, b: String): String {
    var res = ""
    var remainder = 0
    val length = maxOf(a.length, b.length)
    val rpa = a.padStart(length, '0').reversed()
    val rpb = b.padStart(length, '0').reversed()
    for ((da, db) in rpa.zip(rpb)) {
        val curr = da.snafuDigitToInt() + db.snafuDigitToInt() + remainder
        when {
            curr in -2..2 -> {
                remainder = 0
                res = curr.toSnafuChar() + res
            }
            curr in -5..-3 -> {
                remainder = -1
                res = (curr + 5).toSnafuChar() + res
            }
            curr in 3..5 -> {
                remainder = 1
                res = (curr - 5).toSnafuChar() + res
            }
            else -> throw IllegalStateException("remainder: $remainder, da: $da, db: $db")
        }
    }
    if (remainder == 1) {
        res = "1$res"
    } else check (remainder == 0)
    return res
}

fun snafuToDec(snafu: String): Long = snafu.reversed().mapIndexed { i, d ->
    pows[i] * d.snafuDigitToInt()
}.sum()
fun Char.snafuDigitToInt() =  when (this) {
    '0' -> 0
    '1' -> 1
    '2' -> 2
    '-' -> -1
    '=' -> -2
    else -> throw IllegalArgumentException("Got digit $this")
}
fun Int.toSnafuChar() = when (this) {
    -2 -> '='
    -1 -> '-'
    0 -> '0'
    1 -> '1'
    2 -> '2'
    else -> throw IllegalArgumentException("Converting to snafu digit $this")
}
fun decToSnafu(n: Long): String {
    fun recursively(currPow: Long, currValue: Long, remainingPows: List<Long>, accumulatedVal: String): String {
        for (currDigit in listOf(-2, -1, 0, 1, 2) ) {
            val valueWithCurrDigit = currPow * currDigit + currValue
            if (valueWithCurrDigit == n) return accumulatedVal + currDigit.toSnafuChar() + remainingPows.map { "0" }.joinToString("")
            val maxFromRest = remainingPows.sumOf { it * 2 }
            val minFromRest = remainingPows.sumOf { it * -2 }
            if ((valueWithCurrDigit + maxFromRest) >= n && (valueWithCurrDigit + minFromRest) <= n) return recursively(remainingPows.last(), valueWithCurrDigit, remainingPows.dropLast(1), accumulatedVal + currDigit.toSnafuChar())
        }
        throw IllegalStateException("target: $n currValue: $currValue currPow: $currPow remainingPows: $remainingPows accumulatedVal: $accumulatedVal")
    }
    return recursively(pows.last(), 0, pows.dropLast(1), "").dropWhile { it == '0' }
}