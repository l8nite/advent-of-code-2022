import kotlin.math.abs
import kotlin.math.max
import kotlin.system.measureTimeMillis

class Sensor(val x: Int, val y: Int, val beacon: Beacon, var c: Char = 'S') {
    private val distance = abs(x - beacon.x) + abs(y - beacon.y)

    val minX = x - distance
    val maxX = x + distance
    val minY = y - distance
    val maxY = y + distance

    val xRanges: Map<Int,IntRange> = ((y-distance)..(y+distance)).associateWith {
        (x - distance + abs(y - it))..(x + distance - abs(y - it))
    }

    fun inRange(tx: Int, ty: Int): Boolean {
        return xRanges[ty]?.contains(tx) ?: false
    }
}
class Beacon(val x: Int, val y: Int)
fun main() {
    fun parse(input: String): Sensor {
        val r = "^Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)".toRegex()
        val (sx, sy, bx, by) = r.matchEntire(input)!!.destructured
        return Sensor(sx.toInt(), sy.toInt(), Beacon(bx.toInt(), by.toInt()), '@')
    }

    fun draw(sensors: List<Sensor>, highlight: Sensor? = null, rowY: Int? = null) {
        val minX = highlight?.minX ?: sensors.minOf { it.minX }
        val maxX = highlight?.maxX ?: sensors.maxOf { it.maxX }
        val minY = highlight?.minY ?: sensors.minOf { it.minY }
        val maxY = highlight?.maxY ?: sensors.maxOf { it.maxY }

        for (y in minY..maxY) {
            if (rowY != null && (y < rowY - 1 || y > rowY + 1)) {
                continue
            }

            for (x in minX..maxX) {
                if (sensors.any { it.x == x && it.y == y }) {
                    print("S")
                }
                else if (sensors.any { it.beacon.x == x && it.beacon.y == y }) {
                    print("B")
                }
                else if (highlight == null && sensors.any { it.inRange(x, y) }) {
                    print("#")
                }
                else if (highlight != null && highlight.inRange(x, y)) {
                    print("#")
                }
                else {
                    print(".")
                }
            }
            print("\n")
        }
    }

    fun part1(input: List<String>, y: Int): Int {
        val sensors = input.map { parse(it) }
        val minX = sensors.minOf { it.minX }
        val maxX = sensors.maxOf { it.maxX }

        // draw(sensors, null, y)

        var count = 0
        for (x in minX.rangeTo(maxX)) {
            if(sensors.any { it.inRange(x, y) } && sensors.none { it.beacon.x == x && it.beacon.y == y }) {
                count++
            }
        }

        return count
    }

    fun part2(input: List<String>, sx: Int = 20, sy: Int = 20): Long {
        val sensors = input.map { parse(it) }.sortedBy { it.y }

        for (y in 0..sy) {
            val ranges = sensors.filter { it.xRanges.containsKey(y) }.map { it.xRanges[y]!! }.sortedBy { it.first }
            var marker = max(ranges.first().last, 0)
            ranges.drop(1).forEach { range ->
                if (range.first <= marker) {
                    marker = max(range.last, marker)
                } else {
                    return (range.first - 1) * 4000000L + y
                }
            }
        }

        return -1
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day15_test")
    var elapsed = measureTimeMillis {
        check(part1(testInput, 10) == 26)
        check(part2(testInput) == 56000011L)
    }
    println("Completed test inputs in ${elapsed}ms")

    val input = readInput("Day15")
    elapsed = measureTimeMillis {
        println(part1(input, 2000000))
        println(part2(input, 4000000, 4000000))
    }
    println("Completed Day15 inputs in ${elapsed}ms")
}
