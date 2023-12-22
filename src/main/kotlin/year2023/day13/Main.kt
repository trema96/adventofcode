package year2023.day13

import java.io.File

fun main() {
    val input = File("./data/2023/input_13.txt").readText().split("\n\n").map { it.split("\n").filter { it.isNotBlank() } }
    input.sumOf { map ->
        val cols = map.findSymmetryColumns()
        val rows = map.findSymmetryRows()
        if (cols.size > 1) println("Found ${cols.size} cols")
        if (rows.size > 1) println("Found ${rows.size} rows")
        if ((cols+rows).isEmpty()) println("Found no symmetry:\n${map.joinToString("\n")}")
        cols.sum() + rows.sumOf { it * 100 }
    }.also { println(it) }
    input.sumOf { map ->
        val originalCols = map.findSymmetryColumns()
        val originalRows = map.findSymmetryRows()
        map.deSmudged().firstNotNullOf { desmudgedMap ->
            val cols = desmudgedMap.findSymmetryColumns().filterNot { it in originalCols }
            val rows = desmudgedMap.findSymmetryRows().filterNot { it in originalRows }
            if (cols.size > 1) println("Found ${cols.size} cols")
            if (rows.size > 1) println("Found ${rows.size} rows")
            if (cols.size + rows.size > 0)
                cols.sum() + rows.sumOf { it * 100 }
            else
                null
        }
    }.also { println(it) }
}

fun List<String>.findSymmetryColumns(): List<Int> =
    (1 until first().length).filter {  i ->
        val maxSize = minOf(i, first().length - i)
        val leftLines = this.map { it.take(i).takeLast(maxSize) }
        val rightLines = this.map { it.drop(i).take(maxSize) }
        leftLines.zip(rightLines).all {
            it.first == it.second.reversed()
        }
    }

fun List<String>.findSymmetryRows(): List<Int> =
    (1 until size).filter {  i ->
        val maxSize = minOf(i, size - i)
        val topLines = this.take(i).takeLast(maxSize)
        val bottomLines = this.drop(i).take(maxSize)
        topLines == bottomLines.reversed()
    }

fun List<String>.deSmudged(): List<List<String>> = indices.flatMap { i ->
    val pre = take(i)
    val post = drop(i + 1)
    get(i).deSmudged().map { pre + it + post }
}

fun String.deSmudged(): List<String> = indices.map { i ->
    replaceRange(i, i + 1, if (get(i) == '#') "." else "#")
}