package year2015.day1

import java.io.File

fun main() {
    File("./data/2015/input_1.txt").readText()
        .filter { it == '(' || it == ')'}
        .foldIndexed(0) { i, acc, c ->
            val res = if (c == '(') acc + 1 else acc - 1
            if (res == -1) println(i + 1)
            res
        }
        .also { println(it) }
}
