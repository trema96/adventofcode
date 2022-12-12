package year2022.day10

import java.io.File

sealed interface Operation {
    val cycles: Int

    companion object {
        fun parse(line: String): Operation = when {
            line == "noop" -> Noop
            line.startsWith("addx") -> Addx(line.split(" ").last().toInt())
            else -> throw IllegalArgumentException(line)
        }
    }

    object Noop : Operation {
        override val cycles: Int = 1

        override fun toString(): String = "noop"
    }

    class Addx(val quantity: Int) : Operation {
        override val cycles: Int = 2

        override fun toString(): String = "addx $quantity"
    }
}

fun List<Operation>.remap() =
    flatMap { op ->
        List(op.cycles - 1) { Operation.Noop } + op
    }

fun List<Operation>.computeRemappedToCycle(cycle: Int) =
    take(cycle - 1).fold(1) { X, op ->
        when (op) {
            Operation.Noop -> X
            is Operation.Addx -> X + op.quantity
        }
    }

fun List<Operation>.computePrintLocations(): List<Boolean> =
    fold(listOf<Boolean>() to 1) { (acc, X), op ->
        val visible = (acc.size % 40) in (X - 1 .. X + 1)
        (acc + visible) to when (op) {
            Operation.Noop -> X
            is Operation.Addx -> X + op.quantity
        }
    }.first

fun main() {
    val input = File("./data/2022/input_10.txt").readLines()
    val remappedInput = input.map(Operation.Companion::parse).remap()
    println(listOf(20, 60, 100, 140, 180, 220).sumOf { remappedInput.computeRemappedToCycle(it) * it })
    remappedInput.computePrintLocations().forEachIndexed { i, visible ->
        if (i % 40 == 0) println()
        print(if (visible) "#" else ".")
    }
}
