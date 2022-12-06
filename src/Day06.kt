import java.util.ArrayDeque
fun main() {

    fun detectSOT(signal: String, uniqueSize: Int = 4): Int {
        val queue = object : ArrayDeque<Char>(uniqueSize) {
            override fun push(e: Char) {
                if (this.size == uniqueSize) {
                    super.removeLast()
                }
                super.push(e)
            }

            fun isUnique() : Boolean {
                return (this.toSet().size == this.size)
            }
        }

        val end = signal.takeWhile {
            queue.push(it)
            queue.size < uniqueSize || !queue.isUnique()
        }

        return end.length + 1 // inclusive of the final match
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
