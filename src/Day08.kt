fun main() {
    fun forest(input: List<String>) = input.map { r -> r.map { it.digitToInt() } }
    fun trees(forest: List<List<Int>>) = forest.flatMapIndexed { y, r -> r.mapIndexed { x, h -> Triple(x, y, h) } }

    // the 4 top-level sequences the 4 directions we can travel (left, right, up, down), the inner sequence is the
    // height of each tree in that direction from the current point to the edge
    fun lineOfSight(forest: List<List<Int>>, x: Int, y: Int) = listOf(
        listOf((x - 1 downTo 0), (x + 1 until forest[y].size)).map { it.map { x -> x to y } },
        listOf((y - 1 downTo 0), (y + 1 until forest.size)).map { it.map { y -> x to y } }
    ).flatten().map { it.map { (x, y) -> forest[y][x] } }

    fun viewingDistance(los: List<Int>, height: Int) = los.indexOfFirst { it >= height }.let {
        if (it == -1) los.size else it + 1 // -1 means we hit the edge, otherwise add 1 to index for the distance
    }

    fun part1(input: List<String>): Int {
        val forest = forest(input)
        return trees(forest).count { (x, y, h) ->
            lineOfSight(forest, x, y).any { it.all { th -> th < h } }
        }
    }

    fun part2(input: List<String>): Int {
        val forest = forest(input)
        return trees(forest).maxOf { (x, y, h) ->
            lineOfSight(forest, x, y).map { viewingDistance(it, h)}.reduce(Int::times)
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test")
    check(part1(testInput) == 21)
    check(part2(testInput) == 8)

    val input = readInput("Day08")
    println(part1(input))
    println(part2(input))
}
