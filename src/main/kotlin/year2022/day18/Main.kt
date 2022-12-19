package year2022.day18

import java.io.File
import java.util.LinkedList

data class Point3d(val x: Int, val y: Int, val z: Int) {
    fun neighbours(): List<Point3d> = listOf(
        copy(x = x - 1),
        copy(x = x + 1),
        copy(y = y - 1),
        copy(y = y + 1),
        copy(z = z - 1),
        copy(z = z + 1),
    )
}

fun Set<Point3d>.surface() =
    sumOf { it.neighbours().count { n -> n !in this } }

fun main() {
    val points = File("./data/2022/input_18.txt").readLines().map { line ->
        val split = line.split(",").map { it.toInt() }
        Point3d(split[0], split[1], split[2])
    }.toSet()
    println(points.surface())
    val minX = points.minOf { it.x } - 1
    val minY = points.minOf { it.y } - 1
    val minZ = points.minOf { it.z } - 1
    val maxX = points.maxOf { it.x } + 1
    val maxY = points.maxOf { it.y } + 1
    val maxZ = points.maxOf { it.z } + 1
    val sideX = maxX - minX + 1
    val sideY = maxY - minY + 1
    val sideZ = maxZ - minZ + 1
    println(sideX)
    println(sideY)
    println(sideZ)
    val start = Point3d(minX, minY, minZ)
    val wrappingCube = mutableSetOf(start)
    val currExploring = LinkedList(wrappingCube)
    while (currExploring.isNotEmpty()) {
        val curr = currExploring.removeFirst()
        curr.neighbours()
            .filterNot { it.x < minX || it.x > maxX || it.y < minY || it.y > maxY || it.z < minZ || it.z > maxZ }
            .filterNot { it in wrappingCube }
            .filterNot { it in points }
            .forEach {
                currExploring.addLast(it)
                wrappingCube.add(it)
            }
    }
    println("Wrapping done")
    println(wrappingCube.surface() - sideX * sideY * 2 - sideZ * sideY * 2 - sideX * sideZ * 2)
}
