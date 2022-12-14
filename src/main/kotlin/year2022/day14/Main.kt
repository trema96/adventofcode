package year2022.day14

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Scanner

data class Point(val x: Int, val y: Int) {
    fun straightLineTo(other: Point): List<Point> = when {
        this.x == other.x -> absRange(this.y, other.y).map { Point(this.x, it) }
        this.y == other.y -> absRange(this.x, other.x).map { Point(it, this.y) }
        else -> throw IllegalArgumentException("Invalid straight line between points: $x, $y")
    }

    fun fallDownCandidates() = listOf(Point(x, y + 1), Point(x - 1, y + 1), Point(x + 1, y + 1))

    private fun absRange(first: Int, second: Int) =
        if (first < second) first..second else second..first
}

enum class StaticElement(val representation: Char) {
    STONE('#'),
    SAND('o'),
    SOURCE('+')
}

class Field(
    val staticItems: MutableMap<Point, StaticElement>,
    val spawnPoint: Point,
    val maxY: Int,
    val bottomless: Boolean
) {
    var done: Boolean = false
        private set

    var movingSand: Point? = null
        private set

    fun simStep() {
        if (!done) {
            val currSand = movingSand
            if (currSand == null) {
                if (staticItems[spawnPoint] == StaticElement.SAND) {
                    done = true
                } else {
                    movingSand = spawnPoint
                }
            } else if (currSand.y == maxY) {
                movingSand = null
                if (bottomless) {
                    done = true
                } else {
                    staticItems[currSand] = StaticElement.SAND
                }
            } else {
                val nextSand = currSand.fallDownCandidates().firstOrNull { staticItems[it] == null }
                if (nextSand != null) {
                    movingSand = nextSand
                } else {
                    staticItems[currSand] = StaticElement.SAND
                    movingSand = null
                }
            }
        }
    }

    fun representation(firstX: Int, firstY: Int, sizeX: Int, sizeY: Int): List<String> =
        (firstY until (firstY + sizeY)).map { y ->
            String(
                (firstX until (firstX + sizeX)).map { x ->
                    val point = Point(x, y)
                    if (y > maxY) {
                        StaticElement.STONE.representation
                    } else if (point == movingSand) {
                        StaticElement.SAND.representation
                    } else {
                        staticItems[point]?.representation ?: '.'
                    }
                }.toCharArray()
            )
        }
}

fun initField(bottomless: Boolean): Field {
    val input = File("./data/2022/input_14.txt").readLines()
    val allRocks = input.flatMap { line ->
        val coordinates = line.split(" -> ").map { pointString ->
            val split = pointString.split(",")
            Point(split[0].toInt(), split[1].toInt())
        }
        coordinates.zipWithNext().flatMap { it.first.straightLineTo(it.second) }
    }.toSet().associateWith { StaticElement.STONE }
    val sourcePoint = Point(500, 0)
    val maxY = (allRocks.keys + sourcePoint).maxOf { it.y } + 1
    return Field(
        (allRocks + (sourcePoint to StaticElement.SOURCE)).toMutableMap(),
        sourcePoint,
        maxY,
        bottomless
    )
}

suspend fun interactive(terminalSizeX: Int, terminalSizeY: Int) = coroutineScope {
    println("Steps per draw?")
    val stepsPerDraw = readln().toInt()
    val field = initField(false)
    var offsetY = field.spawnPoint.y - 2
    var offsetX = field.spawnPoint.x - terminalSizeX / 2
    launch {
        while (true) {
            val line = readln().lowercase()
            if (line.isNotEmpty()) {
                val intensity = when (line.length) {
                    1 -> 1
                    2 -> 10
                    else -> 50
                }
                when (line[0]) {
                    'w' -> offsetY -= intensity
                    's' -> offsetY += intensity
                    'a' -> offsetX -= intensity
                    'd' -> offsetX += intensity
                }
            }
        }
    }
    while (true) {
        print("\u001b[H\u001b[2J")
        field.representation(offsetX, offsetY, terminalSizeX, terminalSizeY - 1).forEach(::println)
        repeat(stepsPerDraw) { field.simStep() }
        delay(20)
    }
}

fun main() {
    val field = initField(true)
    while (!field.done) field.simStep()
    println(field.staticItems.values.count { it == StaticElement.SAND })
    val fieldBottomless = initField(false)
    while (!fieldBottomless.done) fieldBottomless.simStep()
    println(fieldBottomless.staticItems.values.count { it == StaticElement.SAND })
}
