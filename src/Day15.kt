import kotlin.math.abs
import kotlin.math.max

class MutableIntRange(var first: Int, var last: Int)

fun List<IntRange>.combine(): List<IntRange> {
    val result = mutableListOf<IntRange>()
    var ranger: MutableIntRange? = null

    this.sortedBy { it.first }.forEachIndexed { idx, range ->
        if (ranger == null) { ranger = MutableIntRange(range.first, range.last)}

        if (range.first <= (ranger!!.last+1)) {
            ranger!!.last = max(ranger!!.last, range.last)
        } else {
            result.add(IntRange(ranger!!.first, ranger!!.last))
            ranger!!.first = range.first
            ranger!!.last = range.last
        }
    }

    if (ranger != null) {
        result.add(IntRange(ranger!!.first, ranger!!.last))
    }

    return result
}

fun main() {
    val sensorRegex = "^Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)".toRegex()

    fun parse(input: List<String>): Map<Int, List<IntRange>> {
        val map = mutableMapOf<Int, MutableList<IntRange>>()

        input.map {
            sensorRegex.matchEntire(it)!!.destructured.toList().map(String::toInt)
        }.forEach { (sx, sy, bx, by) ->
            val d = abs(sx - bx) + abs(sy - by)
            val yRange = ((sy - d)..(sy + d))
            yRange.forEach{ y ->
                map.getOrPut(y) { mutableListOf() }.add(
                    (sx - d + abs(sy - y))..(sx + d - abs(sy - y))
                )
            }
        }

        map.replaceAll { y, ranges ->
            ranges.combine() as MutableList<IntRange>
        }

        return map
    }

    fun draw(map: Map<Int, List<IntRange>>, sx: Int? = null, sy: Int? = null) {
        val xMin = map.minOf { it.value.minOf { range -> range.first } }
        val xMax = map.maxOf { it.value.maxOf { range -> range.last } }

        for (y in map.keys.sorted()) {
            for (x in xMin..xMax) {
                var c = '.'
                if (map[y]!!.any { it.contains(x) }) {
                    c = '#'
                }
                if (sx != null && x in 0..sx && (y == 0 || y == sy)) {
                    c = '+'
                }
                if (sy != null && y in 0..sy && (x == 0 || x == sx)) {
                    c = '|'
                }
                print(c)
            }
            print("\n")
        }
    }

    fun part1(input: List<String>, y: Int): Int {
        val map = parse(input)
        if (y == 10) { draw(map) }
        return map[y]!!.sumOf { (it.last - it.first) }
    }

    fun part2(input: List<String>, sx: Int = 20, sy: Int = 20): Long {
        val map = parse(input)
        map.keys.sorted().filter { it in 0..sy }.forEach { y ->
            if(map[y]!!.size > 1) {
                return (map[y]!![1].first - 1) * 4000000L + y
            }
        }

        return -1
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day15_test")
        check(part1(testInput, 10) == 26)
        check(part2(testInput) == 56000011L)

    val input = readInput("Day15")
        println(part1(input, 2000000))
        println(part2(input, 4000000, 4000000))
}
