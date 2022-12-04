fun main() {
    fun part1(input: List<String>): Int {
        var overlapped = 0

        input.forEach { line ->
            val (a1, a2, b1, b2) = line.split("-",",").map { it.toInt() }
            if ((a1 <= b1 && a2 >= b2) || (a1 >= b1 && a2 <= b2) ) {
                overlapped += 1
            }
        }

        return overlapped
    }

    fun part2(input: List<String>): Int {
        var overlapped = 0

        input.forEach { line ->
            val (a1, a2, b1, b2) = line.split("-",",").map { it.toInt() }
            if ((a1 in b1..b2) || (b1 in a1..a2)) {
                overlapped += 1
            }
        }

        return overlapped    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test")
    check(part1(testInput) == 2)
    check(part2(testInput) == 4)

    val input = readInput("Day04")
    println(part1(input))
    println(part2(input))
}
