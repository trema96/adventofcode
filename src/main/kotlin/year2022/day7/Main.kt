package year2022.day7

import java.io.File

class Dir(
    val subDirs: MutableMap<String, Dir>,
    val files: MutableMap<String, Int>,
    val parent: Dir?
) {
    val size: Int by lazy {
        files.values.sum() + subDirs.values.sumOf { it.size }
    }

    fun sequence(): Sequence<Dir> =
        sequenceOf(this) + subDirs.values.asSequence().flatMap { it.sequence() }
}

fun main() {
    val lines = File("./data/2022/input_7.txt").readLines()
    val commandsToOut = mutableListOf<Pair<String, MutableList<String>>>()
    lines.forEach { line ->
        if (line.startsWith("$")) {
            commandsToOut += line to mutableListOf()
        } else {
            commandsToOut.last().second += line
        }
    }
    val root = Dir(mutableMapOf(), mutableMapOf(), null)
    var currentDir = root
    commandsToOut.forEach { (command, out) ->
        if (command.startsWith("$ cd")) {
            check(out.isEmpty())
            currentDir = when (val changeTo = command.drop("$ cd ".length)) {
                "/" -> root
                ".." -> currentDir.parent!!
                else -> currentDir.subDirs.getValue(changeTo)
            }
        } else {
            check(command == "$ ls")
            out.forEach { fileOrDir ->
                val split = fileOrDir.split(" ")
                check(split.size == 2)
                if (split.first() == "dir") {
                    currentDir.subDirs.computeIfAbsent(split.last()) { Dir(mutableMapOf(), mutableMapOf(), currentDir) }
                } else {
                    currentDir.files[split.last()] = split.first().toInt()
                }
            }
        }
    }
    println(root.sequence().filter { it.size <= 100000 }.map { it.size }.sum())
    val maxSpace = 70000000
    val neededSpace = 30000000
    val extraRequired = neededSpace - (maxSpace - root.size)
    println(root.sequence().map { it.size }.sorted().first { it >= extraRequired })
}
