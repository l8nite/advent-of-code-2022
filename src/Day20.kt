private fun move(index: Int, amounts: MutableList<Pair<Int, Long>>) {
    val indexFrom = amounts.indexOfFirst { (initialIndex) -> initialIndex == index }
    val value = amounts[indexFrom]
    val (_, amount) = value

    if (amount == 0L) return

    amounts.removeAt(indexFrom)
    val indexTo = findIndex(indexFrom, amount, amounts.size)
    amounts.add(indexTo, value)
}

private fun decrypt(inputs: List<Long>, mixes: Int, decryptionKey: Int): Long {
    val amounts = inputs
        .mapIndexed { index, amount -> index to amount * decryptionKey }
        .toMutableList()

    repeat(mixes) {
        (0 until amounts.size).forEach { index ->
            move(index, amounts)
        }
    }

    val startIndex = amounts.indexOfFirst { (_, value) -> value == 0L }

    return listOf(1000L, 2000L, 3000L)
        .map { endIndex -> amounts[findIndex(startIndex, endIndex, amounts.size)] }
        .sumOf { (_, amount) -> amount }
}

private fun findIndex(startIndex: Int, endIndex: Long, size: Int): Int =
    (startIndex + endIndex).mod(size)

fun main() {
    fun part1(input: List<String>): Long {
        val inputs = input.map(String::toLong)
        return decrypt(inputs, 1, 1)
    }

    fun part2(input: List<String>): Long {
        val inputs = input.map(String::toLong)
        return decrypt(inputs, 10, 811589153)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day20_test")
    check(part1(testInput) == 3L)
    println("Part 1 checked out!")
    check(part2(testInput) == 1623178306L)
    println("Part 2 checked out!")

    val input = readInput("Day20")
    println(part1(input))
    println(part2(input))
}
