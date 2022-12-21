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
    private val maxCostByType = robots
        .flatMap { it.cost.toList() }
        .groupBy { it.first }
        .mapValues { (_, v) -> v.maxOf { it.second } }
    private val oreBot = robots.first { it.production == Material.ORE }
    private val clayBot = robots.first { it.production == Material.CLAY }
    private val obsidianBot = robots.first { it.production == Material.OBSIDIAN }
    private val geodeBot = robots.first { it.production == Material.GEODE }

    fun allSimulationsToMinute(maxMinute: Int): Sequence<Simulation> {
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

    fun productionScore(maxMinute: Int): Int {
        fun Simulation.productionScoreInner(
            maxMinute: Int,
            maxClayProduction: Int,
            maxBranching: Int?
        ): Int = if (this.minute == maxMinute) {
            this.materials[Material.GEODE] ?: 0
        } else if ((maxBranching == null || maxBranching > 0)
            && !geodeBot.producibleBy(this)
            && (this.production[Material.CLAY] ?: 0) < maxClayProduction
            && (this.materials[Material.CLAY] ?: 0) >= obsidianBot.cost.getValue(Material.CLAY)
            && clayBot.producibleBy(this)
        ) {
            val nextMaxBranching =
                maxBranching?.let { it - 1 } ?: 5
            val scoreWithClay =
                this.nextMinute(clayBot).productionScoreInner(maxMinute, maxClayProduction, nextMaxBranching)
            val nextForObsidian =
                if (obsidianBot.producibleBy(this)) this.nextMinute(obsidianBot) else this.nextMinute(null)
            val scoreWithObsidian = nextForObsidian.productionScoreInner(maxMinute, maxClayProduction, nextMaxBranching)
            if (scoreWithClay > scoreWithObsidian)
                this.nextMinute(clayBot).productionScoreInner(maxMinute, maxClayProduction, maxBranching)
            else if (obsidianBot.producibleBy(this))
                this.nextMinute(obsidianBot).productionScoreInner(maxMinute, maxClayProduction, maxBranching)
            else
                this.nextMinute(null).productionScoreInner(maxMinute, maxClayProduction, maxBranching)
        } else {
            val next = if ((this.materials[Material.OBSIDIAN] ?: 0) >= geodeBot.cost.getValue(Material.OBSIDIAN)) {
                if (geodeBot.producibleBy(this)) this.nextMinute(geodeBot) else this.nextMinute(null)
            } else if ((this.materials[Material.CLAY] ?: 0) >= obsidianBot.cost.getValue(Material.CLAY)) {
                if (obsidianBot.producibleBy(this)) this.nextMinute(obsidianBot) else this.nextMinute(null)
            } else if (this.production.getValue(Material.ORE) < clayBot.cost.getValue(Material.ORE)) {
                if (oreBot.producibleBy(this)) this.nextMinute(oreBot) else this.nextMinute(null)
            } else if (clayBot.producibleBy(this) && (this.production[Material.CLAY] ?: 0) < maxClayProduction) {
                this.nextMinute(clayBot)
            } else {
                this.nextMinute(null)
            }
            next.productionScoreInner(maxMinute, maxClayProduction, maxBranching)
        }
        val baseMaxClay = obsidianBot.cost.getValue(Material.CLAY)
        return maxOf(
            Simulation.startingSimulation().productionScoreInner(maxMinute, baseMaxClay, null),
            Simulation.startingSimulation().productionScoreInner(maxMinute, baseMaxClay / 2, null),
            Simulation.startingSimulation().productionScoreInner(maxMinute, baseMaxClay / 2 + 1, null)
        )
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

    fun producesMaterialsFor(robotType: RobotType) =
        robotType.cost.keys.all { (production[it] ?: 0) > 0 }
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
            it.allSimulationsToMinute(24).maxOf { s -> s.materials[Material.GEODE] ?: 0 } * it.id
        }
    )
    println(blueprints.take(3).fold(1) { acc, blueprint -> blueprint.productionScore(32) * acc })
}
