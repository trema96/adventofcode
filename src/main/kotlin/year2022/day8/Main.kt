package year2022.day8

import java.io.File

class Forest(private val treesByRow: List<List<Int>>) {
    fun rowAt(index: Int): List<Int> =
        treesByRow[index]

    fun columnAt(index: Int): List<Int> =
        treesByRow.map { it[index] }

    fun treeAt(row: Int, column: Int) =
        treesByRow[row][column]

    fun indices(): Iterable<Pair<Int, Int>> =
        treesByRow.indices.flatMap { row ->
            treesByRow[row].indices.map { row to it }
        }

    fun isVisible(rowIndex: Int, columnIndex: Int): Boolean {
        val row = rowAt(rowIndex)
        val column = columnAt(columnIndex)
        val tree = treeAt(rowIndex, columnIndex)
        return row.take(columnIndex).all { it < tree }
            || row.drop(columnIndex + 1).all { it < tree }
            || column.take(rowIndex).all { it < tree }
            || column.drop(rowIndex + 1).all { it < tree }
    }

    fun viewScore(rowIndex: Int, columnIndex: Int): Int {
        val row = rowAt(rowIndex)
        val column = columnAt(columnIndex)
        val tree = treeAt(rowIndex, columnIndex)

        fun viewScore(view: List<Int>) =
            (view.takeWhile { it < tree }.size + 1).coerceAtMost(view.size)

        val leftView = row.take(columnIndex).reversed()
        val rightView = row.drop(columnIndex + 1)
        val topView = column.take(rowIndex).reversed()
        val bottomView = column.drop(rowIndex + 1)

        return viewScore(leftView) * viewScore(rightView) * viewScore(topView) * viewScore(bottomView)
    }

    override fun toString(): String =
        treesByRow.joinToString("\n") {
            it.joinToString("")
        }
}

fun main() {
    val input = File("./data/2022/input_8.txt").readLines()
    val forest = Forest(input.map { line -> line.map { c -> c.toString().toInt() } })
    val visibles = forest.indices().count { (row, column) ->
        forest.isVisible(row, column)
    }
    val bestView = forest.indices().map { (row, column) ->
        forest.viewScore(row, column)
    }.maxOrNull()
    println(visibles)
    println(bestView)
}
