val programs = listOf<Pair<String, suspend (Int, Int) -> Unit>>(
    "09/2022 - Rope simulator" to { _, _ -> year2022.day9.interactive() },
    "14/2022 - Sand pyramid" to { lines, columns -> year2022.day14.interactive(columns, lines) },
)

suspend fun main(args: Array<String>) {
    println("Make your selection:")
    programs.forEachIndexed { i, (programName, _) ->
        println("[$i] $programName")
    }
    val selection = readln().toInt()
    print("\u001b[H\u001b[2J")
    programs[selection].second(args[0].toInt(), args[1].toInt())
}
