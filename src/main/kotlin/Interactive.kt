val programs = listOf<Pair<String, suspend () -> Unit>>(
    "09/2022 - Rope simulator" to { year2022.day9.interactive() }
)

suspend fun main() {
    println("Make your selection:")
    programs.forEachIndexed { i, (programName, _) ->
        println("[$i] $programName")
    }
    val selection = readln().toInt()
    print("\u001b[H\u001b[2J")
    programs[selection].second()
}
