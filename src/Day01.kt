fun main() {
    fun parseElves(input: List<String>): List<Int> {
        return input.fold(mutableListOf(0)) { elves, line ->
            elves.apply {
                if (line.isBlank()) add(0) else this[size - 1] += line.toInt()
            }
        }
    }

    // find the highest-calorie elf
    fun part1(input: List<String>): Int {
        return parseElves(input).max()
    }

    fun part2(input: List<String>): Int {
        return parseElves(input).sortedDescending().take(3).sum()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 24000)
    check(part2(testInput) == 45000)

     val input = readInput("Day01")
     println(part1(input))
     println(part2(input))
}
