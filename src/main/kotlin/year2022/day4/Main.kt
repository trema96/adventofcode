package year2022.day4

import java.io.File

fun main() {
    val ranges = File("./data/2022/input_4.txt").readLines().map { line ->
        val rangesList = line.split(",").map { rangeString ->
            val split = rangeString.split("-")
            check(split.size == 2)
            (split[0].toInt() .. split[1].toInt()).toSet()
        }
        check(rangesList.size == 2)
        rangesList[0] to rangesList[1]
    }
    println(ranges.count { it.first.containsAll(it.second) || it.second.containsAll(it.first) })
    println(ranges.count { it.first.intersect(it.second).isNotEmpty() })
}