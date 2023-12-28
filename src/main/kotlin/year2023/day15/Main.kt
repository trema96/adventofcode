package year2023.day15

import utils.indexed
import java.io.File

fun main() {
    val input = File("./data/2023/input_15.txt").readLines().first().split(",")
    input.sumOf { asciiHash(it) }.also { println(it) }
    calculateConfigurationPower(arrangeLenses(input).also { println(it) }).also { println(it) }
}

fun asciiHash(string: String): Int =
    string.toByteArray(Charsets.US_ASCII).fold(0) { acc, curr ->
        ((acc + curr) * 17) % 256
    }

fun arrangeLenses(instructions: List<String>): Map<Int, Map<String, Int>> {
    val res = mutableMapOf<Int, LinkedHashMap<String, Int>>()
    instructions.forEach { instruction ->
        if (instruction.endsWith('-')) {
            val instructionLabel = instruction.dropLast(1)
            res[asciiHash(instructionLabel)]?.remove(instructionLabel)
        } else {
            val (instructionLabel, value) = instruction.split(("[=-]").toRegex()).let { it[0] to it[1].toInt() }
            res.getOrPut(asciiHash(instructionLabel)) { LinkedHashMap() }[instructionLabel] = value
        }
    }
    return res
}

fun calculateConfigurationPower(config: Map<Int, Map<String, Int>>): Long =
    config.toList().sumOf { (box, lenses) ->
        lenses.toList().indexed().sumOf { (index, lensInfo) ->
            (box + 1) * (index + 1) * lensInfo.second.toLong()
        }
    }