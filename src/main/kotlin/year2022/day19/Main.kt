package year2022.day19

import java.io.File

enum class Material(val tier: Int) {
    ORE(1),
    CLAY(2),
    OBSIDIAN(3),
    GEODE(4)
}

data class RobotType(
    val cost: Map<Material, Int>,
    val production: Material
) {
    fun producedExactlyBy(simulation: Simulation): Boolean {
        val resourcesAvailable = producibleBy(simulation)
        if (!resourcesAvailable) return false
        val resourcesNotAvailablePreviously =
            cost.any { (material, quantity) -> (simulation.history.last().first.materials[material] ?: 0) < quantity }
        if (resourcesNotAvailablePreviously) return true
        return simulation.history.last().second != null
    }

    fun producibleBy(simulation: Simulation): Boolean =
        cost.all { (material, quantity) -> (simulation.materials[material] ?: 0) >= quantity }

    fun willBeProducibleBy(simulation: Simulation) =
        cost.any { (material, quantity) -> (simulation.materials[material] ?: 0) < quantity }
            && cost.all { (material, _) -> (simulation.production[material] ?: 0) > 0 }
}

data class Blueprint(
    val id: Int,
    val robots: List<RobotType>
) {
    val maxCostByType = robots
        .flatMap { it.cost.toList() }
        .groupBy { it.first }
        .mapValues { (_, v) -> v.maxOf { it.second } }

    fun simulateToMinute(maxMinute: Int): Sequence<Simulation> {
        fun extendRecursively(currSimulation: Simulation): Sequence<Simulation> =
            if (currSimulation.minute == maxMinute) {
                sequenceOf(currSimulation)
            } else {
                val currentProducible =
                    robots.filter { it.producedExactlyBy(currSimulation) }
                val geodeRobot = currentProducible.firstOrNull { it.production == Material.GEODE }
                if (geodeRobot != null) {
                    extendRecursively(currSimulation.nextMinute(geodeRobot))
                } else {
                    val currentProducibleNextSteps = currentProducible.asSequence()
                        .filterNot { robotType ->
                            val p = robotType.production
                            (currSimulation.production[p] ?: 0) >= maxCostByType.getValue(p)
                        }
                        .map { currSimulation.nextMinute(it) }
                    val allNextRobots =
                        if (robots.any { it.willBeProducibleBy(currSimulation) })
                            currentProducibleNextSteps + currSimulation.nextMinute(null)
                        else
                            currentProducibleNextSteps
                    allNextRobots.flatMap { extendRecursively(it) }
                }
            }
        return extendRecursively(Simulation.startingSimulation())
    }
}

class Simulation(
    val minute: Int,
    val production: Map<Material, Int>,
    val materials: Map<Material, Int>,
    val history: List<Pair<Simulation, RobotType?>>
) {
    companion object {
        fun startingSimulation() = Simulation(0, mapOf(Material.ORE to 1), emptyMap(), emptyList())
    }

    fun nextMinute(newBot: RobotType?): Simulation {
        val materialsPlusCurrProduction = (materials.toList() + production.toList())
            .groupBy { it.first }
            .mapValues { (_, v) -> v.sumOf { it.second } }
        return if (newBot == null) {
            Simulation(minute + 1, production, materialsPlusCurrProduction, history + (this to null))
        } else {
            val newProduction = production + (newBot.production to (production[newBot.production]?.let { it + 1 } ?: 1))
            val materialsMinusCost = materialsPlusCurrProduction.mapValues { (material, quantity) ->
                newBot.cost[material]?.let { quantity - it } ?: quantity
            }
            Simulation(minute + 1, newProduction, materialsMinusCost, history + (this to newBot))
        }
    }

    override fun toString(): String {
        return "Simulation(minute=$minute, production=$production, materials=$materials)"
    }
}

fun main() {
    val blueprints = File("./data/2022/input_19.txt").readLines().map { line ->
        val numbers = line.split(" ").mapNotNull { it.filterNot { c -> c == ':' }.toIntOrNull() }
        val oreBot = RobotType(mapOf(Material.ORE to numbers[1]), Material.ORE)
        val clayBot = RobotType(mapOf(Material.ORE to numbers[2]), Material.CLAY)
        val obsidianBot = RobotType(mapOf(Material.ORE to numbers[3], Material.CLAY to numbers[4]), Material.OBSIDIAN)
        val geodeBot = RobotType(mapOf(Material.ORE to numbers[5], Material.OBSIDIAN to numbers[6]), Material.GEODE)
        Blueprint(numbers[0], listOf(oreBot, clayBot, obsidianBot, geodeBot))
    }
    println(
        blueprints.sumOf {
            it.simulateToMinute(24).maxOf { s -> s.materials[Material.GEODE] ?: 0 } * it.id
        }
    )
}