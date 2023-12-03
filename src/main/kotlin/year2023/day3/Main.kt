package year2023.day3

import java.io.File

data class PartNumber(
    val row: Int,
    val startCol: Int,
    val endCol: Int, //Inclusive
    val value: Int
) {
    fun isAdjacentTo(r: Int, c: Int) =
        r in (this.row - 1 .. this.row + 1) && c in (this.startCol - 1 .. this.endCol + 1)
}

fun main() {
    val map = File("./data/2023/input_3.txt").readLines()
    val maxCol = map.first().length - 1
    val maxRow = map.size - 1
    val numbers = map.flatMapIndexed {  row, line ->
        val indexedLine = line.mapIndexed { col, c -> col to c }
        val foundParts = mutableListOf<PartNumber>()
        var remainingLine = indexedLine.dropWhile { !it.second.isDigit() }
        while (remainingLine.isNotEmpty()) {
            val currPartComponents = remainingLine.takeWhile { it.second.isDigit() }
            foundParts.add(
                PartNumber(
                    row,
                    currPartComponents.first().first,
                    currPartComponents.last().first,
                    currPartComponents.joinToString("") { it.second.toString() }.toInt()
                )
            )
            remainingLine = remainingLine.drop(currPartComponents.size).dropWhile { !it.second.isDigit() }
        }
        foundParts
    }
    val validPartNumbers = numbers.filter { pn ->
        val rowIndices = maxOf(0, pn.row - 1) .. minOf(maxRow, pn.row + 1)
        val colIndices = maxOf(0, pn.startCol - 1) .. minOf(maxCol, pn.endCol + 1)
        rowIndices.any { r ->
            colIndices.any { c ->
                map[r][c].let { it != '.' && !it.isDigit() }
            }
        }
    }
    println(validPartNumbers.sumOf { it.value })
    val gearRatios = map.flatMapIndexed { row, line ->
        line.mapIndexedNotNull { col, c ->
            if (c == '*') {
                val adjacentParts = validPartNumbers.filter { it.isAdjacentTo(row, col) }
                if (adjacentParts.size == 2) {
                    adjacentParts[0].value * adjacentParts[1].value
                } else null
            } else null
        }
    }
    println(gearRatios.sum())
}