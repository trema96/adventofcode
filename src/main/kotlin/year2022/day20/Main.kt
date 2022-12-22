package year2022.day20

import java.io.File
import java.util.*

class DoublyLinkedNode(val id: Int, val value: Int) {
    var next: DoublyLinkedNode? = null
    var prev: DoublyLinkedNode? = null

    fun moveForward() {
        val oldPrev = prev!!
        val oldNext = next!!
        oldPrev.next = oldNext
        oldNext.prev = oldPrev
        this.prev = oldNext
        this.next = oldNext.next
        next!!.prev = this
        oldNext.next = this
    }

    fun moveBackwards() {
        val oldPrev = prev!!
        val oldNext = next!!
        oldPrev.next = oldNext
        oldNext.prev = oldPrev
        this.next = oldPrev
        this.prev = oldPrev.prev
        prev!!.next = this
        oldPrev.prev = this
    }

    override fun toString(): String {
        return "DoublyLinkedNode(id=$id, value=$value, next=${next?.id}, prev=${prev?.id})"
    }
}

fun mixInPlace(mixOrder: List<DoublyLinkedNode>, amount: (DoublyLinkedNode) -> Int) {
    mixOrder.forEach { currToMove ->
        val amountToMove = amount(currToMove)
        if (amountToMove < 0) {
            repeat(-amountToMove) { currToMove.moveBackwards() }
        } else {
            repeat(amountToMove) { currToMove.moveForward() }
        }
    }
}

fun initLinks(idToValue: List<Pair<Int, Int>>): Pair<DoublyLinkedNode, List<DoublyLinkedNode>> {
    val all = idToValue.map { DoublyLinkedNode(it.first, it.second) }
    val lastIndex = all.size - 1
    val zero = all.first { it.value == 0 }
    for (i in (1 until lastIndex)) {
        all[i].prev = all[i - 1]
        all[i].next = all[i + 1]
    }
    all[0].next = all[1]
    all[0].prev = all[lastIndex]
    all[lastIndex].next = all[0]
    all[lastIndex].prev = all[lastIndex - 1]
    return (zero to all)
}

fun getCoordinates(zero: DoublyLinkedNode): List<Int> {
    val coordinates = mutableListOf<Int>()
    var curr = zero.next!!
    for (i in 1 .. 3000) {
        if (i % 1000 == 0) {
            coordinates.add(curr.value)
        }
        curr = curr.next!!
    }
    return coordinates
}

fun main() {
    val input = File("./data/2022/input_20.txt").readLines().mapIndexed { i, v -> i to v.toInt() }
    val (zero, toMove) = initLinks(input)
    mixInPlace(toMove) { it.value }
    println(getCoordinates(zero).sum())
    val (zero2, toMove2) = initLinks(input)
    repeat(10) { mixInPlace(toMove2) { ((it.value * 811589153L) % (input.size - 1)).toInt() } }
    println(getCoordinates(zero2).sumOf { it * 811589153L })
}

fun printFrom(node: DoublyLinkedNode) {
    var curr = node.next!!
    println(node)
    while (curr !== node) {
        println(curr)
        curr = curr.next!!
    }
}
