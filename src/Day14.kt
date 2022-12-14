import kotlin.math.max
import kotlin.math.min

class Location(var x: Int, var y: Int, var type: Char = Type.AIR) {
    override fun toString(): String {
        return "($x, $y)"
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Location)
            return false
        else {
            if (other.x == this.x && other.y == this.y) {
                return true
            }
        }
        return false
    }

    override fun hashCode(): Int = x.hashCode() + y.hashCode()
}

class Type {
    companion object {
        const val ROCK = '#'
        const val SAND = 'o'
        const val AIR = '.'
        const val SOURCE = '+'
    }
}

fun main() {
    val source = Location(500, 0, Type.SOURCE)

    fun parse(input: String): List<Location> {
        return " -> ".toRegex().split(input).map { pair ->
            pair.split(",").let {
                Location(it[0].toInt(), it[1].toInt())
            }
        }
    }

//    fun printCave(cave: MutableSet<Location>) {
//        for (y in cave.minOf { it.y }..cave.maxOf { it.y }) {
//            for (x in cave.minOf { it.x }..cave.maxOf { it.x }) {
//                print(cave.firstOrNull { it.x == x && it.y == y }?.type ?: Type.AIR)
//            }
//            println()
//        }
//    }

    fun drawLine(cave: MutableSet<Location>, list: List<Location>) {
        fun rangeBetween(a: Int, b: Int) = min(a, b) .. max(a, b)

        for (x in rangeBetween(list[0].x, list[1].x)) {
            for (y in rangeBetween(list[0].y, list[1].y)) {
                cave.add(Location(x, y, Type.ROCK))
            }
        }
    }

    fun makeCave(input: List<String>): MutableSet<Location> {
        val cave = mutableSetOf<Location>()
        input.map { parse(it) }.forEach { pairs -> pairs.windowed(2, 1).forEach { drawLine(cave, it) } }
        cave.add(source)
        return cave
    }

    fun below(sand: Location) = Location(sand.x, sand.y + 1)

    fun downLeft(sand: Location) = Location(sand.x - 1, sand.y + 1)

    fun downRight(sand: Location) = Location(sand.x + 1, sand.y + 1)

    fun part1(input: List<String>): Int {
        val cave = makeCave(input)
        val maxY = cave.maxOf { it.y }
        val sand = Location(source.x, source.y, Type.SAND)

        while (sand.y < maxY) {
            if (!cave.contains(below(sand))) {
                sand.y += 1
                continue
            }

            if (!cave.contains(downLeft(sand))) {
                sand.x -= 1
                sand.y += 1
                continue
            }

            if (!cave.contains(downRight(sand))) {
                sand.x += 1
                sand.y += 1
                continue
            }

            // sand comes to rest
            cave.add(Location(sand.x, sand.y, Type.SAND))

            sand.x = source.x
            sand.y = source.y
        }

        // printCave(cave)

        return cave.filter { it.type == Type.SAND }.size
    }

    fun part2(input: List<String>): Int {
        val cave = makeCave(input)
        val maxY = cave.maxOf { it.y }
        val sand = Location(source.x, source.y, Type.SAND)

        while (true) {
            if (sand.y <= maxY && !cave.contains(below(sand))) {
                sand.y += 1
                continue
            }

            if (sand.y <= maxY && !cave.contains(downLeft(sand))) {
                sand.x -= 1
                sand.y += 1
                continue
            }

            if (sand.y <= maxY && !cave.contains(downRight(sand))) {
                sand.x += 1
                sand.y += 1
                continue
            }

            // sand comes to rest
            cave.add(Location(sand.x, sand.y, Type.SAND))

            if (sand == source) {
                break
            } else {
                sand.x = source.x
                sand.y = source.y
            }
        }

        // printCave(cave)

        return cave.filter { it.type == Type.SAND || it.type == Type.SOURCE }.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day14_test")
    check(part1(testInput) == 24)
    check(part2(testInput) == 93)

    val input = readInput("Day14")
    println(part1(input))
    println(part2(input))
}
