package year2022.day5

import java.io.File

data class Field(val columns: List<List<Char>>) {
    companion object {
        fun parse(hSize: Int, data: List<String>): Field {
            val columns = List(hSize) { mutableListOf<Char>() }
            data.forEach { dataLine ->
                columns.forEachIndexed { i, column ->
                    val char = dataLine[1 + i * 4]
                    if (char != ' ') column.add(char)
                }
            }
            return Field(columns)
        }
    }

    fun move(quantity: Int, from: Int, to: Int, reverse: Boolean): Field {
        check(columns[from - 1].size >= quantity)
        return Field(columns.mapIndexed { i, column ->
            when (i + 1) {
                from -> column.drop(quantity)
                to -> columns[from - 1].take(quantity).let { if (reverse) it.reversed() else it } + column
                else -> column
            }
        })
    }
}

fun main() {
    val lines = File("./data/2022/input_5.txt").readLines()
    val fieldData = lines.takeWhile { it.isNotBlank() }.dropLast(1)
    val movesData = lines.dropWhile { it.isNotBlank() }.drop(1)
    val initialField = Field.parse(9, fieldData)

    fun calculateEndField(reverse: Boolean): String {
        val res = movesData.fold(initialField) { field, moveLine ->
            val params = moveLine.split(" ").mapNotNull { it.toIntOrNull() }
            check(params.size == 3)
            field.move(params[0], params[1], params[2], reverse)
        }
        return res.columns.joinToString("") { it.first().toString() }
    }

    println(calculateEndField(true))
    println(calculateEndField(false))
}
