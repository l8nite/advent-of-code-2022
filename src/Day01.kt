fun main() {
    fun parseElves(input: List<String>): List<Int> {
        val elves = mutableListOf<Int>()
        var total = 0

        input.forEach { calories ->
            if (calories.isBlank()) {
                elves.add(total)
                total = 0
            } else {
                total += calories.toInt()
            }
        }

        elves.add(total)

        return elves
    }

    // find the highest-calorie elf
    fun part1(input: List<String>): Int {
        return parseElves(input).maxOf { it }
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
