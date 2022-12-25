fun Triple<Int,Int,Any>.toPair(): Pair<Int, Int> {
    return Pair(this.first, this.second)
}

enum class Day24Direction(val vector: Vector, val char: Char) {
    D(Vector(0, +1), 'v'),
    R(Vector(+1, 0), '>'),
    L(Vector(-1, 0), '<'),
    U(Vector(0, -1), '^'),
    W(Vector(0, 0), '@')
}

fun directionFrom(c: Char): Day24Direction? {
    return when (c) {
        '>' -> Day24Direction.R
        '<' -> Day24Direction.L
        '^' -> Day24Direction.U
        'v' -> Day24Direction.D
        else -> null
    }
}

fun positionAt(time: Int, blizzard: Triple<Int,Int,Day24Direction>, xRange: IntRange, yRange: IntRange): Pair<Int,Int> {
    val dx = blizzard.third.vector.x * time
    val dy = blizzard.third.vector.y * time
    return wrapIndex(blizzard.first + dx, blizzard.second + dy, xRange, yRange)
}

fun blizzardAt(time: Int, blizzard: Triple<Int,Int,Day24Direction>, xRange: IntRange, yRange: IntRange): Triple<Int,Int, Day24Direction> {
    val pos = positionAt(time, blizzard, xRange, yRange)
    return Triple(pos.first, pos.second, blizzard.third)
}

fun wrapIndex(x: Int, y: Int, xRange: IntRange, yRange: IntRange): Pair<Int, Int> {
    val xSize = (xRange.last + 1)
    val ySize = (yRange.last + 1)
    return Pair((xSize + x % xSize) % xSize, (ySize + y % ySize) % ySize)
}

fun availableAt(time: Int, expedition: Pair<Int,Int>, destination: Pair<Int,Int>, blizzards: List<Triple<Int,Int,Day24Direction>>, xRange: IntRange, yRange: IntRange): Set<Triple<Int,Int,Int>> {
    val unsafe = blizzards.map { positionAt(time, it, xRange, yRange) }.toSet()

    // filter out moves that aren't in the grid, but include a move "off the grid" if it is the destination
    val moves = Day24Direction.values().filter {
        (it == Day24Direction.W && expedition != destination) || // always allow waiting, even if we're "off the grid" (at the start)
        ((expedition.first + it.vector.x) in xRange && (expedition.second + it.vector.y) in yRange) ||
        (expedition.first + it.vector.x == destination.first && expedition.second + it.vector.y == destination.second)
    }.mapNotNull {
        val position = Triple(expedition.first + it.vector.x, expedition.second + it.vector.y, time)
        if (!unsafe.contains(position.toPair())) {
            position
        } else { null }
    }.toMutableSet()

    return moves
}

fun main() {
    fun drawMap(
        time: Int,
        expedition: Pair<Int, Int>,
        destination: Pair<Int, Int>,
        blizzards: List<Triple<Int, Int, Day24Direction>>,
        xRange: IntRange,
        yRange: IntRange
    ) {
        print("#") // left border
        for (x in xRange) {
            if (expedition.first == x && expedition.second == -1) { print("E") } else { print("#") }
        }
        println("#") // right border

        val blizzardsAt = blizzards.map { blizzardAt(time, it, xRange, yRange) }
        val blizzardsCount = blizzardsAt.groupingBy { Pair(it.first, it.second) }.eachCount()

        for (y in yRange) {
            print("#") // left border
            for (x in xRange) {
                val loc = Pair(x, y)
                if (expedition == loc) {
                    print("E")
                } else if (blizzardsCount.containsKey(loc)) {
                    if (blizzardsCount[loc]!! == 1) {
                        print(blizzardsAt.first { Pair(it.first, it.second) == loc }.third.char)
                    } else {
                        print(blizzardsCount[loc])
                    }
                } else {
                    print(".")
                }
            }
            println("#") // right border
        }

        print("#") // left border
        for (x in xRange) {
            if (destination.first == x) { print(".") } else { print("#") }
        }
        println("#\n") // right border
    }

    fun explore(
        startTime: Int,
        expedition: Pair<Int, Int>,
        destination: Pair<Int, Int>,
        blizzards: List<Triple<Int, Int, Day24Direction>>,
        xRange: IntRange,
        yRange: IntRange
    ): Int {
        // BFS
        val toExplore = mutableListOf<Triple<Int, Int, Int>>()
        val explored = mutableSetOf(Triple(expedition.first, expedition.second, 0)) // x, y, time
        val parents = mutableMapOf<Triple<Int, Int, Int>, Triple<Int, Int, Int>>() // x,y,time -> x,y,time
        toExplore.addAll(availableAt(startTime, expedition, destination, blizzards, xRange, yRange))

        var bestPath: Triple<Int, Int, Int>? = null
        while (toExplore.any()) {
            val newLocation = toExplore.removeFirst()
            val time = newLocation.third + 1

            if (newLocation.toPair() == destination) {
                bestPath = newLocation
                break
            }

            val available = availableAt(time, newLocation.toPair(), destination, blizzards, xRange, yRange).filterNot {
                explored.contains(it)
            }

            available.forEach {
                parents[it] = newLocation
                explored.add(it)
                toExplore.add(it)
            }
        }

        //        val history = mutableListOf(bestPath!!)
        //        while (true) {
        //            val node = parents.getOrDefault(history.last(), null)
        //            if (node == null) {
        //                break
        //            } else {
        //                history.add(node)
        //            }
        //        }
        //        history.add(Triple(expedition.first, expedition.second, 0))
        //
        //        history.reversed().forEach {
        //            val pos = it.toPair()
        //            val time = it.third
        //            if (time == 0) {
        //                println("Initial state:")
        //            } else {
        //                println("Minute $time, move to $pos")
        //            }
        //            drawMap(time, pos, destination, blizzards, xRange, yRange)
        //        }

        return bestPath!!.third
    }

    fun part1(input: List<String>): Int {
        val xRange = 0..input.first().lastIndex - 2
        val yRange = 0..input.lastIndex - 2

        // we're going to treat the x/y ranges as the "internal" part of the map, so the start/end are "off the grid"
        val expedition = Pair(input.first().drop(1).indexOfFirst { it == '.' }, -1)
        val destination = Pair(input.last().drop(1).indexOfFirst { it == '.' }, yRange.last + 1)

        val blizzards = input.drop(1).dropLast(1).flatMapIndexed { y, row ->
            row.drop(1).dropLast(1).mapIndexedNotNull { x, c ->
                if (c == '.') {
                    null
                } else {
                    Triple(x, y, directionFrom(c)!!)
                }
            }
        }

        return explore(1, expedition, destination, blizzards, xRange, yRange)
    }

    fun part2(input: List<String>): Int {
        val xRange = 0..input.first().lastIndex - 2
        val yRange = 0..input.lastIndex - 2

        // we're going to treat the x/y ranges as the "internal" part of the map, so the start/end are "off the grid"
        val expedition = Pair(input.first().drop(1).indexOfFirst { it == '.' }, -1)
        val destination = Pair(input.last().drop(1).indexOfFirst { it == '.' }, yRange.last + 1)

        val blizzards = input.drop(1).dropLast(1).flatMapIndexed { y, row ->
            row.drop(1).dropLast(1).mapIndexedNotNull { x, c ->
                if (c == '.') {
                    null
                } else {
                    Triple(x, y, directionFrom(c)!!)
                }
            }
        }

        val trip1 = explore(1, expedition, destination, blizzards, xRange, yRange)
        val trip2 = explore(trip1, destination, expedition, blizzards, xRange, yRange)
        val trip3 = explore(trip2, expedition, destination, blizzards, xRange, yRange)

        return trip3
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day24_test")
    check(part1(testInput) == 18)
    println("Part 1 checked out!")
    check(part2(testInput) == 54)
    println("Part 2 checked out!")

    val input = readInput("Day24")
    println(part1(input))
    println(part2(input))
}
