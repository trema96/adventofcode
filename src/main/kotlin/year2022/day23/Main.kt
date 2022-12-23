package year2022.day23

import java.io.File

enum class CardinalDirection {
    NORTH,
    SOUTH,
    WEST,
    EAST
}

data class Point(val x: Int, val y: Int) {
    private fun around() = (x - 1 .. x + 1).flatMap { x ->
        (y - 1 .. y + 1).map { y -> Point(x, y) }
    }.filterNot { it == this }

    private fun aroundIn(direction: CardinalDirection) = when (direction) {
        CardinalDirection.EAST -> around().filter { it.x == x + 1 }
        CardinalDirection.WEST -> around().filter { it.x == x - 1 }
        CardinalDirection.SOUTH -> around().filter { it.y == y - 1 }
        CardinalDirection.NORTH -> around().filter { it.y == y + 1 }
    }

    private fun pointAt(direction: CardinalDirection) = when (direction) {
        CardinalDirection.NORTH -> copy(y = y + 1)
        CardinalDirection.SOUTH -> copy(y = y - 1)
        CardinalDirection.WEST -> copy(x = x - 1)
        CardinalDirection.EAST -> copy(x = x + 1)
    }

    fun declareMovement(priorities: List<CardinalDirection>, everyone: Set<Point>): Point {
        if (around().all { it !in everyone }) return this
        val preferredDirection = priorities.firstOrNull { direction -> aroundIn(direction).all { it !in everyone } }
        return preferredDirection?.let { pointAt(it) } ?: this
    }
}

class Field(val elves: Set<Point>, val priorities: List<CardinalDirection>) {
    fun move(): Field {
        val declarationsByElf = elves.associateWith { it.declareMovement(priorities, elves) }
        val doubleDeclarations = declarationsByElf.values.groupBy { it }.filter { it.value.size > 1 }.keys
        val newElves = declarationsByElf.map { (og, declared) ->
            if (declared in doubleDeclarations) og else declared
        }.toSet()
        return Field(newElves, priorities.drop(1) + priorities.first())
    }

    fun score(): Int = (elves.minOf { it.x } .. elves.maxOf { it.x }).sumOf { x ->
        (elves.minOf { it.y } .. elves.maxOf { it.y }).count { y ->
            Point(x, y) !in elves
        }
    }

    fun printField() {
        (elves.minOf { it.y } .. elves.maxOf { it.y }).reversed().forEach { y ->
            (elves.minOf { it.x } .. elves.maxOf { it.x }).forEach { x ->
                if (Point(x, y) !in elves) print(".") else print("#")
            }
            println()
        }
    }
}

fun main() {
    val input = File("./data/2022/input_23.txt").readLines().reversed().flatMapIndexed { y, line ->
        line.flatMapIndexed { x, c ->
            if (c == '#') listOf(Point(x, y)) else emptyList()
        }
    }.toSet()
    val initialField = Field(
        input,
        listOf(CardinalDirection.NORTH, CardinalDirection.SOUTH, CardinalDirection.WEST, CardinalDirection.EAST)
    )
    val finalField = (0 until 10).fold(initialField) { prev, _ ->
        prev.move()
    }
    println(finalField.score())
    println(fullSimulationRound(initialField))
}

fun fullSimulationRound(initialField: Field): Int {
    var curr = initialField
    var round = 0
    while (true) {
        round++
        val next = curr.move()
        if (next.elves == curr.elves) return round
        curr = next
    }
}