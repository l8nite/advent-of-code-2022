import kotlin.math.abs

class Vector(val x: Int, val y: Int)

enum class Direction(val vector: Vector) {
    R(Vector(+1, 0)),
    L(Vector(-1, 0)),
    U(Vector(0, -1)),
    D(Vector(0, +1))
}
class Knot(var x: Int, var y: Int) {
    val history = mutableSetOf<Pair<Int, Int>>()

    init {
        history.add(Pair(x, y))
    }

    fun move(v: Vector) {
        x += v.x
        y += v.y
        history.add(Pair(x, y))
    }

    fun follow(k: Knot) {
        if (abs(k.x-x) >= 2 || abs(k.y-y) >=2) {
            x += k.x.compareTo(x)
            y += k.y.compareTo(y)
            history.add(Pair(x, y))
        }
    }

    override fun toString(): String {
        return "($x, $y): History: $history"
    }
}

fun main() {
    fun move(knots: List<Knot>, d: Direction, n: Int) {
        repeat(n) {
            knots[0].move(d.vector)
            knots.drop(1).forEachIndexed { index, knot ->
                knot.follow(knots[index])
            }
        }
    }

    fun parse(input: List<String>): List<Pair<Direction, Int>> = input.map { line ->
        line.split(" ").let {
            Pair(Direction.valueOf(it[0]), it[1].toInt())
        }
    }

    fun part1(input: List<String>): Int {
        val knots = mutableListOf<Knot>(Knot(0,0), Knot(0,0))
        parse(input).forEach { (d, n) -> move(knots, d, n) }
        return knots.last().history.size
    }

    fun part2(input: List<String>): Int {
        val knots = mutableListOf<Knot>()
        repeat(10) { knots.add(Knot(0, 0)) }
        parse(input).forEach { (d, n) -> move(knots, d, n) }
        return knots.last().history.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput1 = readInput("Day09_test_1")
    check(part1(testInput1) == 13)

    val testInput2 = readInput("Day09_test_2")
    check(part2(testInput1) == 1)
    check(part2(testInput2) == 36)

    val input = readInput("Day09")
    println(part1(input))
    println(part2(input))
}
