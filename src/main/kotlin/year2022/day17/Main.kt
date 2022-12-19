package year2022.day17

import java.io.File

data class Point(val x: Int, val y: Int)

data class Rock(val points: List<Point>) {
    fun moveLeft()  = Rock(points.map { it.copy(x = it.x - 1) })

    fun moveDown() = Rock(points.map { it.copy(y = it.y - 1) })

    fun moveRight() = Rock(points.map { it.copy(x = it.x + 1) })

    val inBounds: Boolean get() = points.all { it.x in 0..6 }

    fun collidesWithAnyOf(others: Set<Point>): Boolean =
        points.any { it in others }
}

class RockGenerator {
    private var index = 0

    fun nextRock(highestPoint: Int): Rock {
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
    var highestPoint = -1

    fun simulateRock() {
        var currRock: Rock? = generator.nextRock(highestPoint)
        while (currRock != null) {
            val sideMovementCandidate = if (nextGust() == '<') currRock.moveLeft() else currRock.moveRight()
            if (sideMovementCandidate.inBounds && !sideMovementCandidate.collidesWithAnyOf(fallenRocks)) {
                currRock = sideMovementCandidate
            }
            val downMovementCandidate = currRock.moveDown()
            if (downMovementCandidate.points.any { it.y < 0 } || downMovementCandidate.collidesWithAnyOf(fallenRocks)) {
                fallenRocks += currRock.points
                val highestOfRock = currRock.points.maxOf { it.y }
                if (highestOfRock > highestPoint) highestPoint = highestOfRock
                currRock = null
            } else {
                currRock = downMovementCandidate
            }
        }
    }

    fun nextGust(): Char = gusts[gustIndex].also { gustIndex = (gustIndex + 1) % gusts.size  }
}

fun main() {
    val gusts = File("./data/2022/input_17.txt").readText().toList().filter { it == '<' || it == '>' }
    val volcano = Volcano(gusts)
//    repeat(5) {
//        volcano.simulateRock()
//        val maxY = volcano.fallenRocks.maxOf { it.y }
//        (0 .. maxY).reversed().forEach { y ->
//            (0 .. 6).forEach { x ->
//                if (Point(x, y) in volcano.fallenRocks) print("#") else print(".")
//            }
//            println()
//        }
//        println(volcano.highestPoint)
//    }
    repeat(2022) { volcano.simulateRock() }
    println(volcano.highestPoint + 1)
}
