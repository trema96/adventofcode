package year2023.day14

import utils.indexed
import java.io.File

fun main() {
    val input = File("./data/2023/input_14.txt").readLines().map { it.toList() }
    calculateLoad(rollUp(input)).also { println(it) }
    calculateLoad(repeatRollCycle(input, 1000000000)).also { println(it) }
}

fun repeatRollCycle(map: List<List<Char>>, times: Int): List<List<Char>> {
    val seen = mutableMapOf<List<List<Char>>, Int>()
    var currMap = map
    var i = 0
    while (i < times) {
        if (currMap in seen) {
            val cycleLength = i - seen.getValue(currMap)
            val remaining = (times - i) % cycleLength
            repeat(remaining) { currMap = rollCycle(currMap) }
            return currMap
        }
        seen[currMap] = i
        currMap = rollCycle(currMap)
        i++
    }
    return currMap
}

fun printMap(map: List<List<Char>>) {
    map.forEach { println(it.joinToString("")) }
    println()
}

fun rollCycle(map: List<List<Char>>): List<List<Char>> {
    return rotateClockwise(rollUp(rotateClockwise(rollUp(rotateClockwise(rollUp(rotateClockwise(rollUp(map))))))))
}

fun rotateClockwise(map: List<List<Char>>): List<List<Char>> {
    val newMap = MutableList(map[0].size) { MutableList(map.size) { '.' } }
    map.forEachIndexed { rowIndex, row ->
        row.forEachIndexed { colIndex, spot ->
            newMap[colIndex][map.size - 1 - rowIndex] = spot
        }
    }
    return newMap
}

fun rollUp(map: List<List<Char>>): List<List<Char>> {
    fun rollUpOnce(currMap: List<List<Char>>): List<List<Char>> {
        val newMap = MutableList(currMap.size) { MutableList(currMap[0].size) { '.' } }
        newMap[0] = currMap[0].toMutableList()
        currMap.indexed().drop(1).forEach { (rowIndex, row) ->
            row.toList().indexed().forEach { (colIndex, spot) ->
                if (spot == 'O' && newMap[rowIndex - 1][colIndex] == '.') {
                    newMap[rowIndex - 1][colIndex] = 'O'
                    newMap[rowIndex][colIndex] = '.'
                } else {
                    newMap[rowIndex][colIndex] = spot
                }
            }
        }
        return newMap
    }
    var prev = map
    var curr = rollUpOnce(prev)
    while (curr != prev) {
        prev = curr
        curr = rollUpOnce(prev)
    }
    return curr
}

fun calculateLoad(map: List<List<Char>>): Long =
    map.reversed().indexed().sumOf { (i, line) ->
        line.count { it == 'O' } * (i + 1L)
    }