import kotlin.math.abs

enum class Direction(val v: Pair<Int,Int>) { NORTH(Pair(0, -1)), SOUTH(Pair(0, +1)), EAST(Pair(+1, 0)), WEST(Pair(-1, 0)) }

fun main() {
    fun visibility(forest: List<List<Int>>, x: Int, y: Int): Boolean? {
        return Direction.values().firstNotNullOfOrNull {
            var cx = x
            var cy = y
            var visible = true

            while (
                (it == Direction.NORTH && cy != 0) ||
                (it == Direction.SOUTH && cy != forest.lastIndex) ||
                (it == Direction.WEST && cx != 0) ||
                (it == Direction.EAST && cx != forest[y].lastIndex)
            ) {
                cx += it.v.first
                cy += it.v.second

                if (forest[cy][cx] >= forest[y][x]) {
                    visible = false
                    break
                }
            }

            if (!visible) null else true
        }
    }

    fun part1(input: List<String>): Int {
        val forest = input.map { row -> row.map { it.digitToInt() } }
        var visible = 0
        forest.indices.forEach { y ->
            forest[y].indices.forEach { x ->
                if (visibility(forest, x, y) == true) { visible++ }
            }
        }

        return visible
    }

    fun scenic(forest: List<List<Int>>, x: Int, y: Int): Int {
        return Direction.values().map {
            // count the number of trees of height less than ours in this direction
            var cx = x
            var cy = y
            while (
                (it == Direction.NORTH && cy != 0) ||
                (it == Direction.SOUTH && cy != forest.lastIndex) ||
                (it == Direction.WEST && cx != 0) ||
                (it == Direction.EAST && cx != forest[y].lastIndex)
            ) {
                cx += it.v.first
                cy += it.v.second
                if (forest[cy][cx] >= forest[y][x]) {
                    break
                }
            }

            abs(x-cx) + abs(y-cy)
        }.reduce(Int::times)
    }

    fun part2(input: List<String>): Int {
        val forest = input.map { row -> row.map { it.digitToInt() } }
        return forest.flatMapIndexed { y, row ->
            row.indices.map { x ->
                scenic(forest, x, y)
            }
        }.max()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test")
    check(part1(testInput) == 21)
    check(part2(testInput) == 8)

    val input = readInput("Day08")
    println(part1(input))
    println(part2(input))
}
