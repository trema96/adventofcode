package year2022.day21

import java.io.File
import java.lang.IllegalArgumentException

sealed interface Monkey {
    val name: String

    fun calculateValue(barrel: Map<String, Monkey>): Long

    fun refersToHuman(barrel: Map<String, Monkey>): Boolean

    data class Screaming(
        override val name: String,
        val value: Long
    ): Monkey {
        override fun calculateValue(barrel: Map<String, Monkey>): Long =
            value

        override fun refersToHuman(barrel: Map<String, Monkey>): Boolean =
            false
    }

    data class Calculating(
        override val name: String,
        val operation: Operation
    ): Monkey {
        override fun calculateValue(barrel: Map<String, Monkey>): Long {
            val left = barrel.getValue(operation.left).calculateValue(barrel)
            val right = barrel.getValue(operation.right).calculateValue(barrel)
            return operation.operator.calculate(left, right)
        }

        override fun refersToHuman(barrel: Map<String, Monkey>): Boolean {
            if (operation.left == HUMAN || operation.right == HUMAN) return true
            return barrel.getValue(operation.left).refersToHuman(barrel)
                    || barrel.getValue(operation.right).refersToHuman(barrel)
        }
    }
}

enum class Operator(val calculate: (Long, Long) -> Long) {
    ADD({ a, b -> StrictMath.addExact(a, b) }),
    SUB({ a, b -> StrictMath.subtractExact(a, b) }),
    MUL({ a, b -> StrictMath.multiplyExact(a, b) }),
    DIV({ a, b -> (a / b).also { check(a % b == 0L) { "Not exact division" } } });

    companion object {
        fun parse(string: String) = when (string) {
            "+" -> ADD
            "-" -> SUB
            "*" -> MUL
            "/" -> DIV
            else -> throw IllegalArgumentException(string)
        }
    }
}

data class Operation(val left: String, val operator: Operator, val right: String)

const val HUMAN = "humn"
const val ROOT = "root"

fun main() {
    val barrel = File("./data/2022/input_21.txt").readLines().map { line ->
        val splitLine = line.split(" ")
        val name = splitLine[0].dropLast(1)
        if (splitLine.size == 2) {
            Monkey.Screaming(name, splitLine[1].toLong())
        } else {
            Monkey.Calculating(
                name,
                Operation(splitLine[1], Operator.parse(splitLine[2]), splitLine[3])
            )
        }
    }.associateBy { it.name }
    println(barrel.getValue(ROOT).calculateValue(barrel))
    part2(barrel)
}

fun part2(barrel: Map<String, Monkey>) {
    val rootOperation = (barrel.getValue(ROOT) as Monkey.Calculating).operation

    fun simplifyLeft(target: Long, operator: Operator, operand: Long): Long = when (operator) {
        Operator.ADD -> StrictMath.subtractExact(target, operand)
        Operator.SUB -> StrictMath.subtractExact(operand, target)
        Operator.MUL -> (target / operand).also { check(target % operand == 0L) { "Remainder for division" } }
        Operator.DIV -> (operand / target).also { check(operand % target == 0L) { "Remainder for division" } }
    }

    fun simplifyRight(target: Long, operator: Operator, operand: Long): Long = when (operator) {
        Operator.ADD -> StrictMath.subtractExact(target, operand)
        Operator.SUB -> StrictMath.addExact(target, operand)
        Operator.MUL -> (target / operand).also { check(target % operand == 0L) { "Remainder for division" } }
        Operator.DIV -> StrictMath.multiplyExact(target, operand)
    }


    fun solve(calculatingMonkeyName: String, target: Long): Long {
        val currOp = (barrel.getValue(calculatingMonkeyName) as Monkey.Calculating).operation
        return if (currOp.left == HUMAN) {
            simplifyRight(target, currOp.operator, barrel.getValue(currOp.right).calculateValue(barrel))
        } else if (currOp.right == HUMAN) {
            simplifyLeft(target, currOp.operator, barrel.getValue(currOp.left).calculateValue(barrel))
        } else if (barrel.getValue(currOp.left).refersToHuman(barrel)) {
            solve(
                currOp.left,
                simplifyRight(target, currOp.operator, barrel.getValue(currOp.right).calculateValue(barrel))
            )
        } else {
            solve(
                currOp.right,
                simplifyLeft(target, currOp.operator, barrel.getValue(currOp.left).calculateValue(barrel))
            )
        }
    }

    val res = if (barrel.getValue(rootOperation.left).refersToHuman(barrel)) solve(
        rootOperation.left,
        barrel.getValue(rootOperation.right).calculateValue(barrel)
    ) else solve(
        rootOperation.right,
        barrel.getValue(rootOperation.left).calculateValue(barrel)
    )
    println(res)
}