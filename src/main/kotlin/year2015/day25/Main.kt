package year2015.day25

val start = Entry(1, 1, 20151125)

data class Entry(val row: Int, val column: Int, val value: Long) {
    fun next(): Entry {
        val nextValue = this.value * 252533 % 33554393
        return if (row == 1) {
            Entry(column + 1, 1, nextValue)
        } else {
            Entry(row - 1, column + 1, nextValue)
        }
    }
}

fun main() {
    var curr = start
    while (curr.row != 3010 || curr.column != 3019) {
        curr = curr.next()
    }
    println(curr)
}