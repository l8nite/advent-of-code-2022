fun main() {
    val points = mapOf("Rock" to 1, "Paper" to 2, "Scissors" to 3)
    val beats = mapOf("Rock" to "Paper", "Paper" to "Scissors", "Scissors" to "Rock")
    val beatenBy = beats.entries.associateBy({ it.value }){ it.key }

    fun parseGuide(input: List<String>, strategy: Map<String, String>): List<Pair<String, String>> {
        val opponent = mapOf("A" to "Rock", "B" to "Paper", "C" to "Scissors")
        return input.map { line ->
            line.split(" ").let {
                Pair(opponent[it[0]]!!, strategy[it[1]]!!)
            }
        }
    }

    fun score(theirs: String, ours: String): Int {
        var score = if (theirs == ours) { 3 } else if ( beats[theirs] == ours ) { 6 } else { 0 }
        score += points[ours]!!
        return score
    }

    fun part1(input: List<String>): Int {
        val guide = parseGuide(input, mapOf("X" to "Rock", "Y" to "Paper", "Z" to "Scissors"))
        var score = 0

        guide.forEach {
            score += score(it.first, it.second)
        }

        return score
    }

    fun part2(input: List<String>): Int {
        val guide = parseGuide(input, mapOf("X" to "Lose", "Y" to "Draw", "Z" to "Win"))
        var score = 0

        guide.forEach {
            val (theirs, strategy) = it
            val ours = when (strategy) {
                "Lose" -> beatenBy[theirs]
                "Win" -> beats[theirs]
                "Draw" -> theirs
                else -> null
            }
            score += score(theirs, ours!!)
        }

        return score
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
    check(part1(testInput) == 15)
    check(part2(testInput) == 12)

    val input = readInput("Day02")
    println(part1(input))
    println(part2(input))
}
