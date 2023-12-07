package utils

fun <T> List<T>.indexed(): List<Pair<Int, T>> = mapIndexed { index, t -> index to t }