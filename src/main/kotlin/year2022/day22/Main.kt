package year2022.day22

import java.io.File
import java.lang.IllegalArgumentException
import kotlin.math.abs

enum class Tile {
    EMPTY,
    OUT,
    WALL
}

enum class Direction(val id: Int, val horizontal: Boolean) {
    RIGHT(0, true),
    DOWN(1, false),
    LEFT(2, true),
    UP(3, false);

    fun oppositeTo(other: Direction) = abs(this.id - other.id) == 2

    val opposite: Direction get() = values().first { this.oppositeTo(it) }
}

data class Position(val row: Int, val col: Int, val facing: Direction) {
    fun rotate(direction: String) = copy(
        facing = when (direction) {
            "L" -> if (facing == Direction.RIGHT) Direction.UP else Direction.values().first { it.id == facing.id - 1}
            "R" -> if (facing == Direction.UP) Direction.RIGHT else Direction.values().first { it.id == facing.id + 1 }
            else -> throw IllegalArgumentException(direction)
        }
    )

    fun front(): Position = when (facing) {
        Direction.RIGHT -> copy(col = col + 1)
        Direction.DOWN -> copy(row = row + 1)
        Direction.LEFT -> copy(col = col - 1)
        Direction.UP -> copy(row = row - 1)
    }
}

class MonkeyMap(private val tiles: List<List<Tile>>, private val quadrants: List<Quadrant>) {
    private val maxColSize = tiles.maxOf { it.size }.also { println(it) }

    fun initialPosition() = Position(0, tiles[0].indexOf(Tile.EMPTY), Direction.RIGHT)

    fun Position.move(amount: Int): Position {
        var curr = this
        repeat(amount) {
            val candidate = curr.front().boundedWraparound()
            if (candidate.tile == Tile.WALL) return curr
            curr = candidate
        }
        return curr
    }

    fun Position.cubeMove(amount: Int): Position {
        var curr = this
        repeat(amount) {
            val candidate = if (curr.front().tile != Tile.OUT) {
                curr.front()
            } else {
                curr.outOfQuadrantFront()
            }
            if (candidate.tile == Tile.WALL) return curr
            curr = candidate
        }
        return curr
    }

    private fun Position.boundedWraparound(): Position = if (tile != Tile.OUT) {
        this
    } else if (facing == Direction.UP || facing == Direction.DOWN) {
        var curr = this.rowWraparound()
        while (curr.tile == Tile.OUT) {
            curr = curr.front().rowWraparound()
        }
        curr
    } else {
        var curr = this.colWraparound()
        while (curr.tile == Tile.OUT) {
            curr = curr.front().colWraparound()
        }
        curr
    }

    private fun Position.outOfQuadrantFront(): Position {
        val currQuadrant = this.quadrant
        val (nextQuadrantId, enteringFrom) = currQuadrant.transitions.getValue(this.facing)
        val nextQuadrant = quadrants.first { it.id == nextQuadrantId }
        val ogRelative = currQuadrant.relativeFrom(this)
        val newRelative = if (this.facing == enteringFrom) 49 - ogRelative else ogRelative
        return nextQuadrant.positionAt(newRelative, enteringFrom)
    }

    private fun Position.rowWraparound() = if (row < 0) {
        copy(row = tiles.size - 1)
    } else if (row >= tiles.size) {
        copy(row = 0)
    } else this

    private fun Position.colWraparound() = if (col < 0) {
        copy(col = maxColSize - 1)
    } else if (col >= maxColSize) {
        copy(col = 0)
    } else this

    private val Position.tile: Tile get() = if (row < 0 || col < 0 || row >= tiles.size || col >= tiles[row].size) {
        Tile.OUT
    } else tiles[row][col]

    private val Position.quadrant: Quadrant get() = quadrants.first {
        row >= it.minRow && row < it.maxRow && col >= it.minCol && col < it.maxCol
    }
}

class Quadrant(
    val id: Int,
    val minRow: Int,
    val maxRow: Int,
    val minCol: Int,
    val maxCol: Int,
    val transitions: Map<Direction, Pair<Int, Direction>>
) {
    fun relativeFrom(position: Position): Int = when (position.facing) {
        Direction.RIGHT -> {
            check(position.col == maxCol - 1)
            position.row - minRow
        }
        Direction.DOWN -> {
            check(position.row == maxRow - 1)
            position.col - minCol
        }
        Direction.LEFT -> {
            check(position.col == minCol)
            position.row - minRow
        }
        Direction.UP -> {
            check(position.row == minRow)
            position.col - minCol
        }
    }

    fun positionAt(relative: Int, edge: Direction): Position = when (edge) {
        Direction.RIGHT -> Position(minRow + relative, maxCol - 1, edge.opposite)
        Direction.DOWN -> Position(maxRow - 1, minCol + relative, edge.opposite)
        Direction.LEFT -> Position(minRow + relative, minCol, edge.opposite)
        Direction.UP -> Position(minRow, minCol + relative, edge.opposite)
    }
}

fun main() {
    val lines = File("./data/2022/input_22.txt").readLines()
    val quadrants = listOf(
        Quadrant(
            1,
            0,
            50,
            50,
            100,
            mapOf(
                Direction.LEFT to (5 to Direction.LEFT),
                Direction.UP to (6 to Direction.LEFT)
            )
        ),
        Quadrant(
            2,
            0,
            50,
            100,
            150,
            mapOf(
                Direction.DOWN to (3 to Direction.RIGHT),
                Direction.RIGHT to (4 to Direction.RIGHT),
                Direction.UP to (6 to Direction.DOWN)
            )
        ),
        Quadrant(
            3,
            50,
            100,
            50,
            100,
            mapOf(
                Direction.LEFT to (5 to Direction.UP),
                Direction.RIGHT to (2 to Direction.DOWN)
            )
        ),
        Quadrant(
            4,
            100,
            150,
            50,
            100,
            mapOf(
                Direction.RIGHT to (2 to Direction.RIGHT),
                Direction.DOWN to (6 to Direction.RIGHT)
            )
        ),
        Quadrant(
            5,
            100,
            150,
            0,
            50,
            mapOf(
                Direction.UP to (3 to Direction.LEFT),
                Direction.LEFT to (1 to Direction.LEFT),
            )
        ),
        Quadrant(
            6,
            150,
            200,
            0,
            50,
            mapOf(
                Direction.DOWN to (2 to Direction.UP),
                Direction.RIGHT to (4 to Direction.DOWN),
                Direction.LEFT to (1 to Direction.UP)
            )
        )
    )
    val map = MonkeyMap(lines.takeWhile { it.isNotBlank() }.map { line ->
        line.map { c ->
            when (c) {
                ' ' -> Tile.OUT
                '.' -> Tile.EMPTY
                '#' -> Tile.WALL
                else -> throw IllegalArgumentException(c.toString())
            }
        }
    }, quadrants)
    val directionsString = lines.dropWhile { it.isNotBlank() }.drop(1).first()
    val movementAmounts = Regex("\\d+").findAll(directionsString).map { it.value.toInt() }.toList()
    val rotations = Regex("[LR]").findAll(directionsString).map { it.value }.toList()
    with (map) {
        var currPosition = initialPosition()
        for (i in movementAmounts.indices) {
            println(currPosition)
            currPosition = currPosition.move(movementAmounts[i])
            if (i < rotations.size) currPosition = currPosition.rotate(rotations[i])
        }
        println((currPosition.row + 1) * 1000 + (currPosition.col + 1) * 4 + currPosition.facing.id)
    }
    with (map) {
        var currPosition = initialPosition()
        for (i in movementAmounts.indices) {
            println(currPosition)
            currPosition = currPosition.cubeMove(movementAmounts[i])
            if (i < rotations.size) currPosition = currPosition.rotate(rotations[i])
        }
        println((currPosition.row + 1) * 1000 + (currPosition.col + 1) * 4 + currPosition.facing.id)
    }
}