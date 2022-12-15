package year2022.day15

import java.io.File
import kotlin.math.abs
import kotlin.system.measureTimeMillis

data class Point(val x: Long, val y: Long) {
    fun manhattanDist(other: Point): Long =
        abs(this.x - other.x) + abs(this.y - other.y)
}

data class Sensor(
    val position: Point,
    val beaconPosition: Point
) {
    val range: Long = position.manhattanDist(beaconPosition)
}

fun main() {
    val input = File("./data/2022/input_15.txt").readLines()
    val numRegex = Regex("-?\\d+")
    val sensors = input.map { line ->
        val matches = numRegex.findAll(line).map { it.value.toLong() }.toList()
        check(matches.size == 4)
        Sensor(Point(matches[0], matches[1]), Point(matches[2], matches[3]))
    }
    val beacons = sensors.map { it.beaconPosition }.toSet()
    val minX = sensors.minOf { it.position.x - it.range }
    val maxX = sensors.maxOf { it.position.x + it.range }
    val yPos = 2000000L
    println((minX..maxX).sumOf { x ->
        val currPos = Point(x, yPos)
        when {
            currPos in beacons -> 0L
            sensors.any { currPos.manhattanDist(it.position) <= it.range } -> 1L
            else -> 0L
        }
    })
    val timeToComplete = measureTimeMillis {
        val freePoint = findFree(sensors)
        println(freePoint)
        println(freePoint.x * 4000000L + freePoint.y)
    }
    println("Time to complete find pos: ${ timeToComplete / 1000.0 }")
}

fun findFree(sensors: List<Sensor>): Point {
    var currY = 0L
    val maxCoord = 4000000L
    while (currY < maxCoord) {
        var currX = 0L
        while (currX < maxCoord) {
            val currPos = Point(currX, currY)
            val maxLeftoverDistance = sensors.maxOf { it.range - it.position.manhattanDist(currPos) }
            if (maxLeftoverDistance < 0) return currPos
            currX += maxLeftoverDistance.coerceAtLeast(1)
        }
        currY += 1
    }
    throw IllegalStateException()
}