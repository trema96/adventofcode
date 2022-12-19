package year2022.day17

import java.io.File

data class Point(val x: Long, val y: Long)

data class Rock(val points: List<Point>) {
    fun moveLeft()  = Rock(points.map { it.copy(x = it.x - 1) })

    fun moveDown() = Rock(points.map { it.copy(y = it.y - 1) })

    fun moveRight() = Rock(points.map { it.copy(x = it.x + 1) })

    val inBounds: Boolean get() = points.all { it.x in 0..6 }

    fun collidesWithAnyOf(others: Set<Point>): Boolean =
        points.any { it in others }
}

class RockGenerator {
    var index = 0
        private set

    fun nextRock(highestPoint: Long): Rock {
        val y = highestPoint + 4
        val rock = when (index % 5) {
            0 -> Rock(listOf(
                Point(2, y),
                Point(3, y),
                Point(4, y),
                Point(5, y)
            ))
            1 -> Rock(listOf(
                Point(3, y),
                Point(2, y + 1),
                Point(3, y + 1),
                Point(4, y + 1),
                Point(3, y + 2)
            ))
            2 -> Rock(listOf(
                Point(2, y),
                Point(3, y),
                Point(4, y),
                Point(4, y + 1),
                Point(4, y + 2)
            ))
            3 -> Rock(listOf(
                Point(2, y),
                Point(2, y + 1),
                Point(2, y + 2),
                Point(2, y + 3)
            ))
            4 -> Rock(listOf(
                Point(2, y),
                Point(2, y + 1),
                Point(3, y + 1),
                Point(3, y)
            ))
            else -> throw IllegalStateException("Invalid index: $index")
        }
        index = (index + 1) % 5
        return rock
    }
}

class Volcano(private val gusts: List<Char>) {
    val fallenRocks = mutableSetOf<Point>()
    private val generator = RockGenerator()
    private var gustIndex = 0
    var highestPoint = -1L
        private set

    fun simulateRock(): VolcanicHistory {
        var currRock: Rock = generator.nextRock(highestPoint)
        while (true) {
            val sideMovementCandidate = if (nextGust() == '<') currRock.moveLeft() else currRock.moveRight()
            if (sideMovementCandidate.inBounds && !sideMovementCandidate.collidesWithAnyOf(fallenRocks)) {
                currRock = sideMovementCandidate
            }
            val downMovementCandidate = currRock.moveDown()
            if (downMovementCandidate.points.any { it.y < 0 } || downMovementCandidate.collidesWithAnyOf(fallenRocks)) {
                fallenRocks += currRock.points
                val highestOfRock = currRock.points.maxOf { it.y }
                val prevHighest = highestPoint
                if (highestOfRock > highestPoint) highestPoint = highestOfRock
                return VolcanicHistory(generator.index, gustIndex, highestPoint - prevHighest)
            } else {
                currRock = downMovementCandidate
            }
        }
    }

    fun nextGust(): Char = gusts[gustIndex].also { gustIndex = (gustIndex + 1) % gusts.size  }
}

data class VolcanicHistory(
    val rockState: Int,
    val gustState: Int,
    val dh: Long
) {
    val state = rockState to gustState
}

fun main() {
    val gusts = File("./data/2022/input_17.txt").readText().toList().filter { it == '<' || it == '>' }
    val volcano = Volcano(gusts)
    repeat(2022) { volcano.simulateRock() }
    println(volcano.highestPoint + 1)
    val volcano2 = Volcano(gusts)
    val repetitionSearchSize = 100_000
    val historySample = (0 until repetitionSearchSize).map { volcano2.simulateRock() }
    val repetitionState = historySample.last().state
    val indicesOfRepetition = historySample.withIndex().filter { it.value.state == repetitionState }.map { it.index }
    fun last2SlicesOfSize(size: Int): Pair<List<VolcanicHistory>, List<VolcanicHistory>> {
        val reversed = indicesOfRepetition.reversed()
        val secondSliceStart = reversed[size * 2]
        val secondSliceEnd = reversed[size]
        return Pair(
            historySample.slice((secondSliceEnd + 1)..reversed[0]),
            historySample.slice((secondSliceStart + 1)..secondSliceEnd)
        )
    }
    val sizeOfSlice = (1 until Int.MAX_VALUE).first { last2SlicesOfSize(it).let { (a, b) -> a == b }}
    val repeatingHistory = last2SlicesOfSize(sizeOfSlice).first
    val fullRepeatingHistoryDelta = repeatingHistory.sumOf { it.dh }
    val target = 1_000_000_000_000
    val remaining = target - historySample.size
    val fullRepetitionsDelta = (remaining / repeatingHistory.size) * fullRepeatingHistoryDelta
    val leftoverRepetitionDelta = repeatingHistory.take((remaining % repeatingHistory.size).toInt()).sumOf { it.dh }
    println(volcano2.highestPoint + fullRepetitionsDelta + leftoverRepetitionDelta + 1)
}
