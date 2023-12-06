package year2023.day5

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.system.measureTimeMillis

val input = File("./data/2023/input_5.txt").readLines()
val seeds = input.first().split(" ").drop(1).map { it.toLong() }
val seedToSoil = parseMapping("seed-to-soil")
val soilToFertilizer = parseMapping("soil-to-fertilizer")
val fertilizerToWater = parseMapping("fertilizer-to-water")
val waterToLight = parseMapping("water-to-light")
val lightToTemperature = parseMapping("light-to-temperature")
val temperatureToHumidity = parseMapping("temperature-to-humidity")
val humidityToLocation = parseMapping("humidity-to-location")
val seedToLocation = listOf(
    seedToSoil,
    soilToFertilizer,
    fertilizerToWater,
    waterToLight,
    lightToTemperature,
    temperatureToHumidity,
    humidityToLocation
)

data class Mapping(
    val entries: List<Entry>
) {
    fun map(v: Long) = entries.firstNotNullOfOrNull { it.getMappedOrNull(v) } ?: v

    fun reverseMap(v: Long) = entries.firstNotNullOfOrNull { it.reverseMapOrNull(v) } ?: v
}

fun List<Mapping>.map(v: Long) = fold(v) { acc, mapping -> mapping.map(acc) }
fun List<Mapping>.reverseMap(v: Long) = reversed().fold(v) { acc, mapping -> mapping.reverseMap(acc) }

data class Entry(
    val target: Long,
    val source: Long,
    val range: Long
) {
    fun getMappedOrNull(v: Long) = if (v in (source until source + range)) {
        target + (v - source)
    } else null

    fun reverseMapOrNull(v: Long) = if (v in target until (target + range)) {
        source + (v - target)
    } else null
}

fun parseMapping(mapName: String) = Mapping(
    input.dropWhile { !it.contains(mapName) }.drop(1).takeWhile { it.isNotEmpty() }.also {
        check(it.isNotEmpty())
    }.map { line ->
        val split = line.split(" ")
        Entry(
            split[0].toLong(),
            split[1].toLong(),
            split[2].toLong(),
        )
    }
)

fun main() {
    measureTimeMillis {
        println(seeds.minOf { seedToLocation.map(it) })
        val seedsRanges = seeds.mapIndexedNotNull { index, l ->
            if (index % 2 == 0)
                (l until l + seeds[index + 1])
            else
                null
        }
        val seedsPOIs = seedsRanges.map { seedToLocation.map(it.first) }
        val otherPOIs = seedToLocation.flatMapIndexed { i, currMapping ->
            val remainingMaps = seedToLocation.drop(i)
            currMapping.entries.map { remainingMaps.map(it.source) }
        }
        (seedsPOIs + otherPOIs).filter { location ->
            val seed = seedToLocation.reverseMap(location)
            seedsRanges.any { range -> seed in range }
        }.minOf { it }.also { println(it) }
    }.also { println("Elapsed: $it") }
}