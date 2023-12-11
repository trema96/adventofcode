package year2023.day10

import utils.indexed
import java.io.File
import java.util.LinkedList
import java.util.Queue

enum class Pipe(val connections: List<Direction>, val char: Char) {
    VERTICAL(listOf(Direction.DOWN, Direction.UP), '|'),
    HORIZONTAL(listOf(Direction.LEFT, Direction.RIGHT), '-'),
    TOP_LEFT(listOf(Direction.UP, Direction.LEFT), 'J'),
    TOP_RIGHT(listOf(Direction.UP, Direction.RIGHT), 'L'),
    BOTTOM_LEFT(listOf(Direction.DOWN, Direction.LEFT), '7'),
    BOTTOM_RIGHT(listOf(Direction.DOWN, Direction.RIGHT), 'F');

    companion object {
        fun parse(c: Char): Pipe? = when (c) {
            '|' -> VERTICAL
            '-' -> HORIZONTAL
            'J' -> TOP_LEFT
            'L' -> TOP_RIGHT
            '7' -> BOTTOM_LEFT
            'F' -> BOTTOM_RIGHT
            '.' -> null
            else -> throw IllegalArgumentException("Invalid pipe: $c")
        }
    }
}

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;
}

fun Direction.opposite() = when (this) {
    Direction.UP -> Direction.DOWN
    Direction.DOWN -> Direction.UP
    Direction.LEFT -> Direction.RIGHT
    Direction.RIGHT -> Direction.LEFT
}

data class Coordinate(val row: Int, val col: Int) {
    fun move(direction: Direction): Coordinate = when (direction) {
        Direction.UP -> Coordinate(row - 1, col)
        Direction.DOWN -> Coordinate(row + 1, col)
        Direction.LEFT -> Coordinate(row, col - 1)
        Direction.RIGHT -> Coordinate(row, col + 1)
    }
}

fun List<List<Char>>.pipeAt(c: Coordinate): Pipe? =
    Pipe.parse(this[c.row][c.col])

fun main() {
    val input = File("./data/2023/input_10.txt").readLines().map { it.toMutableList() }.toMutableList()
    val startCoordinate = input.indexed().firstNotNullOf { (rowI, row) ->
        row.toList().indexed().firstNotNullOfOrNull { (colI, p) ->
            if (p == 'S') {
                Coordinate(rowI, colI)
            } else null
        }
    }
    val starts = Direction.values().mapNotNull { d ->
        startCoordinate.move(d).takeIf {
            it.row in (0 until input.size) && it.col in (0 until input.first().size)
        }?.let { input.pipeAt(it) }?.takeIf {
            it.connections.contains(d.opposite())
        }?.let {
            startCoordinate.move(d) to d
        }
    }
    check(starts.size == 2)
    input[startCoordinate.row][startCoordinate.col] = Pipe.values().single { p ->
        starts.all { it.second in p.connections }
    }.char
    println("Start is actually a ${input.pipeAt(startCoordinate)?.char}")
    tailrec fun follow(startPosition: Coordinate, movedThereInDirection: Direction, remainingSteps: Int): Coordinate =
        if (remainingSteps == 0) {
            startPosition
        } else {
            val connections = input.pipeAt(startPosition)!!.connections
            val direction = connections.filterNot { it == movedThereInDirection.opposite() }.single()
            follow(startPosition.move(direction), direction, remainingSteps - 1)
        }
    val firstInfo = starts.first()
    val lastInfo = starts.last()
    val loopPieces = mutableSetOf(startCoordinate, *starts.map { it.first }.toTypedArray())
    (1 until Int.MAX_VALUE).first { steps ->
        follow(firstInfo.first, firstInfo.second, steps).also { loopPieces.add(it) } == follow(lastInfo.first, lastInfo.second, steps).also { loopPieces.add(it) }
    }.also { println(it + 1) }
    val nextToVisit: Queue<Coordinate> = LinkedList()
    val externalGridCorners = mutableSetOf<Coordinate>()
    fun Coordinate.gridCornerInBound() =
        row in (0 .. input.size) && col in (0 .. input.first().size)
    fun visitExternalCorners(currCornerCoordinate: Coordinate) {
        Direction.values().forEach { d ->
            val nextCornerCoordinates = currCornerCoordinate.move(d)
            if (nextCornerCoordinates !in externalGridCorners && nextCornerCoordinates.gridCornerInBound()) {
                val (checkPipePosition, blockingDirection) = when (d) {
                    Direction.UP -> nextCornerCoordinates to Direction.LEFT
                    Direction.DOWN -> currCornerCoordinate to Direction.LEFT
                    Direction.LEFT -> nextCornerCoordinates to Direction.UP
                    Direction.RIGHT -> currCornerCoordinate to Direction.UP
                }
                if (checkPipePosition in loopPieces) {
                    if (blockingDirection !in input.pipeAt(checkPipePosition)!!.connections) {
                        if (externalGridCorners.add(nextCornerCoordinates)) nextToVisit.add(nextCornerCoordinates)
                    }
                } else if (externalGridCorners.add(nextCornerCoordinates)) nextToVisit.add(nextCornerCoordinates)
            }
        }
    }
    println("Exploring external corners")
    visitExternalCorners(Coordinate(0, 0))
    while (nextToVisit.isNotEmpty()) {
        visitExternalCorners(nextToVisit.poll())
    }
    println("Finding inner tiles")
    (0 until input.size).sumOf { r ->
        (0 until input.first().size).count { c ->
            (Coordinate(r, c) !in loopPieces) && listOf(
                Coordinate(r, c),
                Coordinate(r, c + 1),
                Coordinate(r + 1, c),
                Coordinate(r + 1, c + 1),
            ).all { it !in externalGridCorners }
        }
    }.also { println(it) }
}