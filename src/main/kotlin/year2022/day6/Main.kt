package year2022.day6

import java.io.File

fun main() {
    val input = File("./data/2022/input_6.txt").readText()
    println(input.firstMarker(4))
    println(input.firstMarker(14))
}

fun String.firstMarker(markerSize: Int) =
    this.indices.first { i ->
        val currSet = this.drop(i).take(markerSize).toSet()
        currSet.size == markerSize
    } + markerSize
