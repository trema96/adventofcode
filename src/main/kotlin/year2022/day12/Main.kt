package year2022.day12

import java.io.File
import java.util.LinkedList

data class Point(val x: Int, val y: Int) {
    fun neighbours(): List<Point> =
        listOf(this.copy(x = x - 1), this.copy(x = x + 1), this.copy(y = y - 1), this.copy(y = y + 1))
}

fun main() {
    val input = File("./data/2022/input_12.txt").readLines().map { it.toList() }
    val startY = input.indexOfFirst { it.contains('S') }
    val startX = input[startY].indexOf('S')
    val endY = input.indexOfFirst { it.contains('E') }
    val endX = input[endY].indexOf('E')
    val elevationMap = input.map {
        it.map { c ->
            when (c) {
                'S' -> 'a'.code
                'E' -> 'z'.code
                else -> c.code
            }
        }
    }
    val distancesStraight = distanceMap(elevationMap, Point(startX, startY), false)
    println(distancesStraight[endY][endX])
    val distancesReverse = distanceMap(elevationMap, Point(endX, endY), true)
    val allAs = input.flatMapIndexed { y, row ->
        row.flatMapIndexed { x, c ->  if (c == 'a' || c == 'S') listOf(Point(x, y)) else emptyList() }
    }
    println(allAs.minOf { distancesReverse.getAt(it) })
}

fun <T> List<List<T>>.getAt(point: Point): T = this[point.y][point.x]
fun <T> MutableList<MutableList<T>>.setAt(point: Point, value: T) {
    this[point.y][point.x] = value
}

fun distanceMap(elevationMap: List<List<Int>>, start: Point, reverse: Boolean): List<List<Int>> {
    val distanceMap = elevationMap.map {
        it.map { Int.MAX_VALUE }.toMutableList()
    }.toMutableList()
    distanceMap.setAt(start, 0)
    val pointsQueue = LinkedList(listOf(start))
    while (pointsQueue.isNotEmpty()) {
        val currPoint = pointsQueue.removeFirst()
        val currDist = distanceMap.getAt(currPoint)
        currPoint.neighbours().filter {
            it.x >= 0 && it.y >= 0 && it.y < elevationMap.size && it.x < elevationMap.first().size
        }.forEach { neighbourPoint ->
            val elevationOk = if (reverse)
                isElevationOk(elevationMap, neighbourPoint, currPoint)
            else
                isElevationOk(elevationMap, currPoint, neighbourPoint)
            if (elevationOk && distanceMap.getAt(neighbourPoint) > currDist + 1) {
                distanceMap.setAt(neighbourPoint, currDist + 1)
                pointsQueue.addLast(neighbourPoint)
            }
        }
    }
    return distanceMap
}

fun isElevationOk(elevationMap: List<List<Int>>, from: Point, to: Point): Boolean =
    elevationMap.getAt(to) <= elevationMap.getAt(from) + 1
