fun main() {
    fun score(c: Char): Int {
        return if (c.isLowerCase()) {
            c.code - 'a'.code + 1 // 1-26
        } else {
            c.code - 'A'.code + 27 // 27-52
        }
    }

    fun shareChar(list: List<String>): Set<Char> {
        val frequencyMap = mutableMapOf<Char, Int>()

        list.forEach {
            it.toCharArray().toSet().forEach { c ->
                frequencyMap.merge(c, 1) { a, b -> a + b }
            }
        }

        return frequencyMap.filter { it.value == list.size }.keys
    }

    fun part1(input: List<String>): Int {
        var score = 0

        input.forEach {
            score += score(shareChar(it.chunked(it.length/2)).first())
        }

        return score
    }

    fun part2(input: List<String>): Int {
        var score = 0

        input.chunked(3).forEach {
            score += score(shareChar(it).first())
        }

        return score
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 157)
    check(part2(testInput) == 70)

    val input = readInput("Day03")
    println(part1(input))
    println(part2(input))
}
