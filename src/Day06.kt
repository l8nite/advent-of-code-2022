fun main() {
    fun detectSOT(signal: String, size: Int = 4): Int {
        return signal.windowed(size, step=1).indexOfFirst {
            it.toSet().size == size
        } + size
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
