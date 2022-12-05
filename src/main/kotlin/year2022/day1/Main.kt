package year2022.day1

import java.io.File

fun main() {
    val input = File("./data/2022/input_1.txt").readText()
    val caloriesOfElves = input.split("\n\n").filterNot(::emptyLine).map(::convertBackpackString).sorted()
    println(caloriesOfElves.maxOrNull())
    println(caloriesOfElves.takeLast(3).sum())
}

fun convertBackpackString(backpackString: String): Int {
    return backpackString.split("\n").filterNot(::emptyLine).map(::convertBackpackItemString).sum()
}

fun convertBackpackItemString(backpackItem: String): Int {
    return backpackItem.toInt()
}

fun emptyLine(line: String): Boolean {
    return line.isBlank()
}
