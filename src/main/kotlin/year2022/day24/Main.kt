package year2022.day24

import java.io.File
import java.lang.IllegalArgumentException

data class Point(val x: Int, val y: Int) {
    fun move(direction: Direction) = when (direction) {
        Direction.U -> copy(y = y - 1)
        Direction.D -> copy(y = y + 1)
        Direction.L -> copy(x = x - 1)
        Direction.R -> copy(x = x + 1)
    }
}

enum class Direction {
    U,D,L,R;
}

class Blizzard(val position: Point, private val movementDirection: Direction) {
    fun move(rows: Int, cols: Int): Blizzard {
        val newPositionUnbound = position.move(movementDirection)
        val boundedPosition = when {
            newPositionUnbound.y < 0 -> newPositionUnbound.copy(y = rows - 1)
            newPositionUnbound.y >= rows -> newPositionUnbound.copy(y = 0)
            newPositionUnbound.x < 0 -> newPositionUnbound.copy(x = cols - 1)
            newPositionUnbound.x >= cols -> newPositionUnbound.copy(x = 0)
            else -> newPositionUnbound
        }
        return Blizzard(boundedPosition, movementDirection)
    }
}

class Valley(
    val rows: Int,
    val columns: Int,
    val blizzards: List<Blizzard>
) {
    fun next() = Valley(rows, columns, blizzards.map { it.move(rows, columns) })
}

class PositionAnalysis(
    val time: Int,
    val valley: Valley,
    val potentialPositions: Set<Point>,
    val startPoint: Point
) {
    fun next(): PositionAnalysis {
        val nextValley = valley.next()
        val nextBlizzards = nextValley.blizzards.map { it.position }.toSet()
        val nextPotentialsNoBlizzard = potentialPositions + potentialPositions.flatMap { startPosition ->
            Direction.values()
                .map { direction -> startPosition.move(direction) }
                .filter { it.x >= 0 && it.y >= 0 && it.x < valley.columns && it.y < valley.rows }
        }.toSet() + setOf(startPoint)
        return PositionAnalysis(
            time + 1,
            nextValley,
            nextPotentialsNoBlizzard.filterNot { it in nextBlizzards }.toSet(),
            startPoint
        )
    }
}

fun main() {
    val map = File("./data/2022/input_24.txt").readLines().drop(1).dropLast(1).map { it.drop(1).dropLast(1) }
    val blizzards = map.flatMapIndexed { rowIndex, row ->
        row.mapIndexedNotNull { colIndex, data ->
            when (data) {
                '<' -> Direction.L
                '>' -> Direction.R
                '^' -> Direction.U
                'v' -> Direction.D
                '.' -> null
                else -> throw IllegalArgumentException("Unexpected input $data")
            }?.let { Blizzard(Point(colIndex, rowIndex), it) }
        }
    }
    val startValley = Valley(map.size, map.first().length, blizzards)
    var currAnalysis = PositionAnalysis(
        0,
        startValley,
        emptySet(),
        Point(0, 0)
    )
    val expeditionStart = Point(0, 0)
    val expeditionEnd = Point(startValley.columns - 1, startValley.rows - 1)
    while (expeditionEnd !in currAnalysis.potentialPositions) {
        currAnalysis = currAnalysis.next()
    }
    println(currAnalysis.time + 1)
    currAnalysis = PositionAnalysis(
        currAnalysis.time + 1,
        currAnalysis.valley.next(),
        emptySet(),
        expeditionEnd
    )
    while (expeditionStart !in currAnalysis.potentialPositions) {
        currAnalysis = currAnalysis.next()
    }
    println(currAnalysis.time + 1)
    currAnalysis = PositionAnalysis(
        currAnalysis.time + 1,
        currAnalysis.valley.next(),
        emptySet(),
        expeditionStart
    )
    while (expeditionEnd !in currAnalysis.potentialPositions) {
        currAnalysis = currAnalysis.next()
    }
    println(currAnalysis.time + 1)
}
