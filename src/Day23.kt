enum class Day23Direction(val vector: Vector) {
    NORTH(Vector(0, -1)),
    SOUTH(Vector(0, 1)),
    EAST(Vector(1, 0)),
    WEST(Vector(-1, 0)),
    NORTHWEST(Vector(-1, -1)),
    NORTHEAST(Vector(1, -1)),
    SOUTHWEST(Vector(-1, 1)),
    SOUTHEAST(Vector(1, 1))
}
val checks = mapOf(
    Day23Direction.NORTH to listOf(Day23Direction.NORTH, Day23Direction.NORTHEAST, Day23Direction.NORTHWEST),
    Day23Direction.SOUTH to listOf(Day23Direction.SOUTH, Day23Direction.SOUTHEAST, Day23Direction.SOUTHWEST),
    Day23Direction.WEST to listOf(Day23Direction.WEST, Day23Direction.NORTHWEST, Day23Direction.SOUTHWEST),
    Day23Direction.EAST to listOf(Day23Direction.EAST, Day23Direction.NORTHEAST, Day23Direction.SOUTHEAST),
)

class SeedPlanter(var x: Int, var y: Int) {
    fun propose(map: MutableMap<Pair<Int, Int>, SeedPlanter>, scanDirections: List<Day23Direction>): Pair<Int,Int> {
        scanDirections.forEach { d ->
            if (checks[d]!!.none {
                    map.containsKey(Pair(x + it.vector.x, y + it.vector.y))
                }) {
                return Pair(x + d.vector.x, y + d.vector.y)
            }
        }

        return Pair(x, y)
    }

    fun moveTo(map: MutableMap<Pair<Int, Int>, SeedPlanter>, proposal: Pair<Int, Int>) {
        map.remove(Pair(x, y))
        x = proposal.first
        y = proposal.second
        map[Pair(x, y)] = this
    }

    override fun toString(): String {
        return "Elf at ($x, $y)"
    }
}
fun main() {
    fun parseElves(input: List<String>): List<SeedPlanter> {
        return input.flatMapIndexed { y, row ->
            row.mapIndexedNotNull { x, c ->
                if (c == '#') {
                    SeedPlanter(x, y)
                } else {
                    null
                }
            }
        }
    }

    fun drawMap(map: MutableMap<Pair<Int, Int>, SeedPlanter>) {
        val minX = map.minOf { it.key.first } - 1
        val maxX = map.maxOf { it.key.first } + 1
        val minY = map.minOf { it.key.second } - 1
        val maxY = map.maxOf { it.key.second } + 1
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                if (map.containsKey(Pair(x, y))) {
                    print("#")
                } else {
                    print(".")
                }
            }
            println()
        }
    }

    fun part1(input: List<String>): Int {
        val elves = parseElves(input)
        val map = elves.associateBy { Pair(it.x, it.y) }.toMutableMap()

        println("== Initial State==")
        drawMap(map)

        val scanDirections = mutableListOf(Day23Direction.NORTH, Day23Direction.SOUTH, Day23Direction.WEST, Day23Direction.EAST)

        var round = 1
        repeat(10) {
            val moveProposals = mutableMapOf<Pair<Int,Int>, MutableList<SeedPlanter>>()
            elves.filter { elf ->
                Day23Direction.values().any { map.containsKey(Pair(elf.x + it.vector.x, elf.y + it.vector.y)) }
            }.forEach {
                moveProposals.getOrPut(it.propose(map, scanDirections)) { mutableListOf() }.add(it)
            }

            moveProposals.filter { it.value.size == 1 }.forEach { (proposal, elf) ->
                elf.first().moveTo(map, proposal)
            }

            scanDirections.add(scanDirections.removeFirst())

//            println("== End of Round $round ==")
//            drawMap(map)
            round++
        }

        val minX = map.minOf { it.key.first }
        val maxX = map.maxOf { it.key.first }
        val minY = map.minOf { it.key.second }
        val maxY = map.maxOf { it.key.second }

        return (maxX - minX + 1) * (maxY - minY + 1) - elves.size
    }

    fun part2(input: List<String>): Int {
        val elves = parseElves(input)
        val map = elves.associateBy { Pair(it.x, it.y) }.toMutableMap()

        println("== Initial State==")
        drawMap(map)

        val scanDirections = mutableListOf(Day23Direction.NORTH, Day23Direction.SOUTH, Day23Direction.WEST, Day23Direction.EAST)

        var round = 1
        while(true) {
            val moveProposals = mutableMapOf<Pair<Int,Int>, MutableList<SeedPlanter>>()
            elves.filter { elf ->
                Day23Direction.values().any { map.containsKey(Pair(elf.x + it.vector.x, elf.y + it.vector.y)) }
            }.forEach {
                moveProposals.getOrPut(it.propose(map, scanDirections)) { mutableListOf() }.add(it)
            }

            if (moveProposals.isEmpty()) {
                return round
            }

            moveProposals.filter { it.value.size == 1 }.forEach { (proposal, elf) ->
                elf.first().moveTo(map, proposal)
            }

            scanDirections.add(scanDirections.removeFirst())

//            println("== End of Round $round ==")
//            drawMap(map)
            round++
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day23_test")
    check(part1(testInput) == 110)
    println("Part 1 checked out!")
    check(part2(testInput) == 20)
    println("Part 2 checked out!")

    val input = readInput("Day23")
    println(part1(input))
    println(part2(input))
}
