import java.util.ArrayDeque

fun parseStacks(input: List<String>): MutableList<ArrayDeque<String>> {
    val stackIdPattern = Regex("^(\\s+\\d)+$")

    val stacks = mutableListOf(ArrayDeque<String>())
    val crateNameFilter = Regex("[^A-Z]")

    // parse input lines until we reach something that looks like " 1   2   3"
    // iterate those lines backwards, and add the corresponding crates to their stacks
    input.takeWhile { !stackIdPattern.containsMatchIn(it) }.reversed().forEach { line ->
        line.chunked(4).withIndex().forEach {
            if (it.index == stacks.size) { stacks.add(ArrayDeque<String>()) }
            val crate = it.value.replace(crateNameFilter, "")
            if (crate.isNotEmpty()) {
                stacks[it.index].push(crate)
            }
        }
    }

    return stacks
}

fun parseInstructions(input: List<String>): List<List<Int>> {
    val instrPattern = Regex("move (\\d+) from (\\d+) to (\\d+)")

    return input.mapNotNull { instrPattern.find(it) }.map { instr ->
        instr.destructured.toList().map { it.toInt() }
    }
}

fun main() {
    fun part1(input: List<String>): String {
        val stacks = parseStacks(input)

        parseInstructions(input).forEach { instruction ->
            val (n, src, dst) = instruction
            repeat(n) {
                stacks[dst-1].push(stacks[src-1].pop())
            }
        }

        return stacks.joinToString("") { it.peek() }
    }

    fun part2(input: List<String>): String {
        val stacks = parseStacks(input)

        parseInstructions(input).forEach { instruction ->
            val (n, src, dst) = instruction
            stacks[src-1].take(n).reversed().forEach { stacks[dst-1].push(it) }
            repeat(n) { stacks[src-1].pop() } // clear items from src array
        }

        return stacks.joinToString("") { it.peek() }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day05_test")
    check(part1(testInput) == "CMZ")
    check(part2(testInput) == "MCD")

    val input = readInput("Day05")
    println(part1(input))
    println(part2(input))
}
