package year2015.day2

import java.io.File

fun main() {
    val per = File("./data/2015/input_2.txt").readLines().map { shape ->
        val sizes = shape.split("x").map { it.toInt() }
        require(sizes.size == 3)
        val slack = sizes.sorted().take(2).reduce { a, b -> a * b }
        val paper = 2*sizes[0]*sizes[1] + 2*sizes[2]*sizes[1] + 2*sizes[0]*sizes[2] + slack
        val ribbon = sizes.sorted().take(2).sum() * 2 + sizes.reduce { a, b -> a * b }
        paper to ribbon
    }
    println(per.sumOf { it.first })
    println(per.sumOf { it.second })
}
