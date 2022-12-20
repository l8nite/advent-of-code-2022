import kotlin.math.max

const val ORE = 0
const val CLAY = 1
const val OBSIDIAN = 2
const val GEODE = 3

val resourceNames = listOf("ore", "clay", "obsidian", "geode")

val blueprintRegex =
    ("^Blueprint (\\d+): Each ore robot costs (\\d+) ore. " +
            "Each clay robot costs (\\d+) ore. " +
            "Each obsidian robot costs (\\d+) ore and (\\d+) clay. " +
            "Each geode robot costs (\\d+) ore and (\\d+) obsidian.$").toRegex()

data class Blueprint(val id: Int, val costs: List<List<Int>>) {
    val maxResourceCost = listOf(
        costs.maxOf { it[ORE] },
        costs.maxOf { it[CLAY] },
        costs.maxOf { it[OBSIDIAN] },
        Int.MAX_VALUE
    )

    fun printInfo() {
        println("== Blueprint ${id} ==")
        for (robotType in listOf(ORE, CLAY, OBSIDIAN, GEODE)) {
            val costs = costs[robotType].mapIndexedNotNull { resourceType, resourceCost ->
                if (resourceCost > 0) { "$resourceCost ${resourceNames[resourceType]}" } else { null }
            }
            println("  Each ${resourceNames[robotType]} robot costs ${costs.joinToString(" and ")}")
        }
    }

    companion object {
        fun parse(input: String): Blueprint {
            val (id, oreOre, clayOre, obsidianOre, obsidianClay, geodeOre, geodeObsidian) =
                blueprintRegex.matchEntire(input)!!.destructured

            return Blueprint(id.toInt(), ArrayList<List<Int>>(4).also {
                it.add(ORE, listOf(oreOre.toInt(), 0, 0, 0))
                it.add(CLAY,listOf(clayOre.toInt(), 0, 0, 0))
                it.add(OBSIDIAN, listOf(obsidianOre.toInt(), obsidianClay.toInt(), 0, 0))
                it.add(GEODE, listOf(geodeOre.toInt(), 0, geodeObsidian.toInt(), 0))
            })
        }
    }
}

data class State(var blueprint: Blueprint, var minute: Int = 0, val resources: MutableList<Int> = arrayListOf(0, 0, 0, 0), val robots: MutableList<Int> = arrayListOf(1, 0, 0, 0), var robotToBuild: Int = -1, val history: MutableList<Int> = mutableListOf()) {
    fun operate(operation: Int, debug: Boolean = false) {
        robotToBuild = operation // shortcut for replaying history to set robotToBuild
        minute++

        if (debug) { println("== Minute $minute ==") }

        spendResources(debug)
        harvestResources(debug)
        buildRobot(debug)

        if (debug) { println() }

        history.add(robotToBuild)
    }

    private fun spendResources(debug: Boolean) {
        if (robotToBuild < 0) {
            if (debug) {
                println("You are not constructing a robot this time.")
            }
            return
        }

        blueprint.costs[robotToBuild].forEachIndexed { resourceType, resourceCost ->
            resources[resourceType] -= resourceCost
        }

        if (debug) {
            print("Spend ")
            val costs = blueprint.costs[robotToBuild].mapIndexedNotNull { resourceType, resourceCost ->
                if (resourceCost > 0) {
                    "$resourceCost ${resourceNames[resourceType]}"
                } else {
                    null
                }
            }
            print(costs.joinToString(" and "))
            println(" to start building an ${resourceNames[robotToBuild]}-collecting robot.")
        }
    }

    private fun harvestResources(debug: Boolean) {
        robots.forEachIndexed { robotType, robotCount ->
            resources[robotType] += robotCount
            if (debug && robotCount > 0) {
                println("$robotCount ${resourceNames[robotType]}-collecting robot(s) collects $robotCount ${resourceNames[robotType]}; you now have ${resources[robotType]} ${resourceNames[robotType]}")
            }
        }
    }

    private fun buildRobot(debug: Boolean) {
        if (robotToBuild < 0) {
            return
        }

        robots[robotToBuild]++

        if (debug) {
            println("The new ${resourceNames[robotToBuild]}-collecting robot is ready; you now have ${robots[robotToBuild]} of them.")
        }
    }

    fun canBuildRobot(robotType: Int): Boolean {
        blueprint.costs[robotType].forEachIndexed { resourceType, resourceCost ->
            if (resources[resourceType] < resourceCost) {
                return false
            }
        }

        return true
    }

    fun clone(): State {
        return State(blueprint, minute, resources.toMutableList(), robots.toMutableList(), robotToBuild, history.toMutableList())
    }
}

var statesExplored = 0
var statesExploredFully = 0
var statesPrunedByTriangularNumber = 0
var bestState = mutableMapOf<Int, State>()
var bestCount = mutableMapOf<Int, Int>()
var maxMinutes = 24

fun quality(currentState: State): Int {
    val currentBestCount = bestCount[currentState.blueprint.id] ?: Int.MIN_VALUE

    // increase time, spend the resources for a robot build (if any), harvest resources, build the robot (if any)
    currentState.operate(currentState.robotToBuild)

    // if we've advanced to the end...
    if (currentState.minute == maxMinutes) {
        statesExploredFully++

        if (currentState.resources[GEODE] > currentBestCount) {
            bestCount[currentState.blueprint.id] = currentState.resources[GEODE]
            bestState[currentState.blueprint.id] = currentState.clone()
        }

        return currentState.resources[GEODE]
    }

    // determine if this particular state will ever be able to compete with the best state we've seen
    val timeRemaining = maxMinutes - currentState.minute
    val upperBoundGeodes = currentState.resources[GEODE] + // the existing resources
            (1 until timeRemaining).sum() + // if we made 1 geode-bot on every tick
            (currentState.robots[GEODE] * timeRemaining)   // the ones we will harvest in future rounds

    if (currentBestCount >= upperBoundGeodes) {
        statesPrunedByTriangularNumber++
        return Int.MIN_VALUE // abandon this path...
    }

    val statesToExplore = mutableListOf(
        currentState.clone().apply { robotToBuild = -1 }
    )

    for (robotType in listOf(ORE, CLAY, OBSIDIAN, GEODE)) {
        // TODO: optimize by calculating maximum demand for remaining minutes and see if we already have it covered
        if (currentState.canBuildRobot(robotType) && currentState.robots[robotType] < currentState.blueprint.maxResourceCost[robotType]) {
            statesToExplore.add(currentState.clone().apply { robotToBuild = robotType })
        }
    }

    statesExplored += statesToExplore.size

    // reverse so we try things in order:  geode bots, obsidian bots, clay bots, ore bots, no bots
    return statesToExplore.reversed().maxOf {
        quality(it)
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        val blueprints = input.map { Blueprint.parse(it) }

        val quality = blueprints.sumOf {
            it.printInfo()
            val x = max(0, quality(State(it))) * it.id
            println("QUALITY SCORE: $x")
            println("Explored $statesExploredFully states fully out of $statesExplored total (pruned $statesPrunedByTriangularNumber by triangular number)")
            x
        }

        return quality
    }

    fun part2(input: List<String>): Int {
        val blueprints = input.map { Blueprint.parse(it) }.take(3)

        maxMinutes = 32
        val qualities = blueprints.map {
            it.printInfo()
            val x = max(0, quality(State(it)))
            println("QUALITY SCORE: $x")
            println("Explored $statesExploredFully states fully out of $statesExplored total (pruned $statesPrunedByTriangularNumber by triangular number)")
            x
        }

        return qualities.reduce { acc, q -> q * acc }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day19_test")
    check(part1(testInput) == 33)
    println("Part 1 checked out ok!")
    check(part2(testInput) == 3472)
    println("Part 2 checked out ok!")

    val input = readInput("Day19")
//    println(part1(input))
    println(part2(input))
}
