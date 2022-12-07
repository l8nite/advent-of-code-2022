fun main() {
    fun detectSOT(signal: String, size: Int = 4): Int {
        signal.windowed(size, step=1).forEachIndexed { index, chunk ->
            if (chunk.toSet().size == size) { return index + size }
        }

        return -1
    }

    fun part1(input: List<String>): Int {
        return detectSOT(input.first())
    }

    fun part2(input: List<String>): Int {
        return detectSOT(input.first(), 14)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day06_test")
    check(part1(testInput) == 7)
    check(part2(testInput) == 19)

    val input = readInput("Day06")
    println(part1(input))
    println(part2(input))
}
