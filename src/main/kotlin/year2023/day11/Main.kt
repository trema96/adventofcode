package year2023.day11

import utils.indexed
import java.io.File

data class Coordinate(val row: Int, val col: Int)

fun rangeUntilIgnoreOrder(a: Int, b: Int) =
    if (a > b) b until a else a until b

fun main() {
    val input = File("./data/2023/input_11.txt").readLines()
    val rowsToExpand = input.mapIndexedNotNull { index, s -> index.takeIf { s.all { it == '.' } } }.toSet()
    val colsToExpand = (0 until input.first().length).filter { i -> input.all { it[i] == '.' } }.toSet()
    println(rowsToExpand)
    println(colsToExpand)
    val galaxiesCoordinates = input.flatMapIndexed { rowI, row ->
        row.mapIndexedNotNull { colI, p ->
            if (p == '#') Coordinate(rowI, colI) else null
        }
    }
    fun calculateDistance(expansion: Long): Long =
        galaxiesCoordinates.indexed().sumOf { (i, g1) ->
            galaxiesCoordinates.drop(i + 1).sumOf { g2 ->
                val colIterator = rangeUntilIgnoreOrder(g1.col, g2.col)
                val rowIterator = rangeUntilIgnoreOrder(g1.row, g2.row)
                colIterator.sumOf {
                    if (it in colsToExpand) expansion else 1
                } + rowIterator.sumOf {
                    if (it in rowsToExpand) expansion else 1
                }
            }
        }
    println(calculateDistance(2))
    println(calculateDistance(10))
    println(calculateDistance(100))
    println(calculateDistance(1000000))
}