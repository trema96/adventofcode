package year2022.day9

import java.io.File
import java.lang.IllegalArgumentException
import java.util.LinkedList
import java.util.Locale

data class Point(val x: Int, val y: Int) {
    fun aroundFull(): Set<Point> =
        ((x - 1) .. (x + 1)).flatMap { ax ->
            ((y - 1) .. (y + 1)).map { ay ->
                Point(ax, ay)
            }
        }.toSet()

    fun distance2(other: Point): Int {
        val dx = other.x - this.x
        val dy = other.y - this.y
        return dx * dx + dy * dy
    }

    fun move(direction: String): Point = when (direction) {
        "U" -> this.copy(y = this.y + 1)
        "D" -> this.copy(y = this.y - 1)
        "R" -> this.copy(x = this.x + 1)
        "L" -> this.copy(x = this.x - 1)
        else -> throw IllegalArgumentException(direction)
    }

    fun follow(to: Point): Point =
        if (this in to.aroundFull())
            this
        else
            this.aroundFull().intersect(to.aroundFull()).minByOrNull { it.distance2(to) }!!
}

class Rope(val head: Point, val tail: Point) {
    fun move(direction: String): Rope {
        val newHead = head.move(direction)
        val newTail = tail.follow(newHead)
        return Rope(newHead, newTail)
    }

    override fun toString(): String {
        return "Rope(head=$head, tail=$tail)"
    }
}

class LongRope(val positions: List<Point>) {
    fun move(direction: String): LongRope {
        return LongRope(positions.drop(1).fold(listOf(positions.first().move(direction))) { acc, point ->
            acc + point.follow(acc.last())
        })
    }
}

fun main() {
    val input = File("./data/2022/input_9.txt").readLines()
    val start = Rope(Point(0, 0), Point(0, 0))
    val movements = input.flatMap { line ->
        val direction = line.split(" ").first()
        val repeats = line.split(" ").last().toInt()
        List(repeats) { direction }
    }
    val allPositions = movements.fold(listOf(start)) { acc, movement ->
        acc + acc.last().move(movement)
    }
    println(allPositions.map { it.tail }.toSet().size)
    val startLong = LongRope(List(10) { Point(0, 0) })
    val allPositionsLong = movements.fold(listOf(startLong)) { acc, movement ->
        acc + acc.last().move(movement)
    }
    println(allPositionsLong.map { it.positions.last() }.toSet().size)
}

fun interactive() {
    val rowsSize = 50
    val colsSize = 200
    var curr = LongRope(List(10) { Point(0, 0) })
    var offsetX = - colsSize / 2
    var offsetY = - rowsSize / 2
    val history = LinkedList<LongRope>()
    while (true) {
        print("\u001b[H\u001b[2J")
        System.out.flush()
        val dataMap = curr.positions.withIndex().reversed()
            .groupBy { it.value }
            .mapValues { e -> e.value.minByOrNull { it.index }!!.index }
        (0 until rowsSize).forEach { row ->
            (0 until colsSize).forEach { column ->
                val currPoint = Point(offsetX + column, offsetY + row)
                dataMap[currPoint]?.also { print(it) } ?: print(".")
            }
            println()
        }
        when (readln().uppercase(Locale.getDefault())) {
            "A" -> curr = curr.also { history.addLast(it) }.move("L")
            "S" -> curr = curr.also { history.addLast(it) }.move("U")
            "W" -> curr = curr.also { history.addLast(it) }.move("D")
            "D" -> curr = curr.also { history.addLast(it) }.move("R")
            "DD" -> offsetX -= 1
            "AA" -> offsetX += 1
            "WW" -> offsetY -= 1
            "SS" -> offsetY += 1
            "Z" -> curr = history.removeLast()
        }
    }
}
