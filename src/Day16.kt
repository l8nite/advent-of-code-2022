import kotlin.system.measureTimeMillis

data class Valve(val id: String, val flowRate: Int = 0, val neighbors: List<String>) {
    companion object {
        private val valveRegex = "^Valve (\\w+) has flow rate=(\\d+); tunnels? leads? to valves? ((?:\\w+(?:, )?)+)$".toRegex()

        fun from(line: String): Valve {
            val result = valveRegex.matchEntire(line)
            val id = result!!.groups[1]!!.value
            val rate = result.groups[2]!!.value.toInt()
            val neighbors = result.groups[3]!!.value.split(", ")

            return Valve(id, rate, neighbors)
        }
    }

    override fun toString(): String {
        return "$id ($flowRate) -> $neighbors"
    }
}

data class Path(val valves: List<Valve>, val opened: Map<Valve, Int>, val maxTime: Int = 30) {
    fun currentValve(): Valve = valves.last()

    fun flowTotal(): Int = opened.map { (valve, time) -> (maxTime - time) * valve.flowRate }.sum()

    fun shouldOpenValve(): Boolean {
        return currentValve().flowRate > 0 && !opened.containsKey(currentValve())
    }
}

data class TrainedElephantPath(val elfValves: List<Valve>, val elephantValves: List<Valve>, val opened: Map<Valve, Int>, val maxTime: Int = 26) {
    fun elfCurrentValve(): Valve = elfValves.last()
    fun elephantCurrentValve(): Valve = elephantValves.last()

    fun flowTotal(): Int = opened.map { (valve, time) -> (maxTime - time) * valve.flowRate }.sum()

    fun shouldElfOpenValve(): Boolean {
        return elfCurrentValve().flowRate > 0 && !opened.containsKey(elfCurrentValve())
    }

    fun shouldElephantOpenValve(): Boolean {
        return elephantCurrentValve().flowRate > 0 && !opened.containsKey(elephantCurrentValve())
    }
}

fun main() {
    fun part1(input: List<String>): Int {
        val valves = input.map { Valve.from(it) }.associateBy { it.id }

        // we start with a single path to explore, with a single valve in the path, and nothing opened
        var paths = listOf(
            Path(mutableListOf(valves["AA"]!!), HashMap())
        )

        var bestPath = paths.first()

        // start walking...
        var time = 1
        while (time < 30) {
            val exploratoryPaths = mutableListOf<Path>()

            // evaluate each path we've started walking on and create a new path to explore for each case:
            //  1. we open the current valve (advance time by 1, stay in same position)
            //  2. we move to any of the next valves instead
            for (path in paths) {
                if (path.shouldOpenValve()) {
                    // create a new exploratory path where we advanced time by 1 tick and opened this valve
                    exploratoryPaths.add(
                        Path(path.valves + path.currentValve(), path.opened.toMutableMap().also { it[path.currentValve()] = time })
                    )
                }

                exploratoryPaths.addAll(path.currentValve().neighbors.map { neighbor ->
                    Path(path.valves + valves[neighbor]!!, path.opened.toMap())
                })
            }

            paths = exploratoryPaths.sortedByDescending { it.flowTotal() }.take(10000) // arbitrary
            if (paths.first().flowTotal() > bestPath.flowTotal()) {
                bestPath = paths.first()
            }

            time++
        }

        return bestPath.flowTotal()
    }

    fun part2(input: List<String>): Int {
        val valves = input.map { Valve.from(it) }.associateBy { it.id }

        // we start with a single path to explore, with a single valve in the path, and nothing opened
        val start = listOf(valves["AA"]!!)
        var paths = listOf(
            TrainedElephantPath(start, start, HashMap())
        )

        var bestPath = paths.first()

        // start walking...
        var time = 1
        while (time < 26) {
            val exploratoryPaths = mutableListOf<TrainedElephantPath>()

            for (path in paths) {
                val opened = path.opened.toMutableMap()

                // if one (or both) of them is stopping to open the valve, then there is no need to map
                // every elf position to every elephant position or vice-versa
                if (path.shouldElfOpenValve() || path.shouldElephantOpenValve()) {
                    val possibleElfValves = if (path.shouldElfOpenValve()) {
                        opened[path.elfCurrentValve()] = time
                        listOf(path.elfValves + path.elfCurrentValve())
                    } else {
                        path.elfCurrentValve().neighbors.map { neighbor -> path.elfValves + valves[neighbor]!! }
                    }

                    val possibleElephantValves = if (path.shouldElephantOpenValve()) {
                        opened[path.elephantCurrentValve()] = time
                        listOf(path.elfValves + path.elephantCurrentValve())
                    } else {
                        path.elephantCurrentValve().neighbors.map { neighbor -> path.elephantValves + valves[neighbor]!! }
                    }

                    for (elfValves in possibleElfValves) {
                        for (elephantValves in possibleElephantValves) {
                            exploratoryPaths.add(TrainedElephantPath(elfValves, elephantValves, opened))
                        }
                    }
                }

                // we also need to explore the paths where neither elf nor elephant open a valve
                val combinedPaths = path.elfCurrentValve().neighbors.flatMap { elf ->
                    path.elephantCurrentValve().neighbors.map { elephant ->
                        elf to elephant
                    }
                }.filter { (a, b) -> a != b } // filter out paths from/to the same valve
                    .map { (elf, elephant) ->
                        TrainedElephantPath(path.elfValves + valves[elf]!!, path.elephantValves + valves[elephant]!!, path.opened.toMap() )
                    }

                exploratoryPaths.addAll(combinedPaths)

            }

            paths = exploratoryPaths.sortedByDescending { it.flowTotal() }.take(10000) // arbitrary
            if (paths.first().flowTotal() > bestPath.flowTotal()) {
                bestPath = paths.first()
            }

            time++
        }

        return bestPath.flowTotal()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day16_test")
    check(part1(testInput) == 1651)
    check(part2(testInput) == 1707)

    val input = readInput("Day16")
    var result: Any

    val elapsed1 = measureTimeMillis {
        result = part1(input)
    }
    println("Part 1 Result: $result")
    println("Part 1 Time: ${elapsed1}ms")

    val elapsed2 = measureTimeMillis {
        result = part2(input)
    }
    println("Part 2 Result: $result")
    println("Part 2 Time: ${elapsed2}ms")
}