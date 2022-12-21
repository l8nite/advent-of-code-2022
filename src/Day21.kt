class BetterMonkey(val id: String, var leftMonkey: BetterMonkey? = null, var operator: String? = null, var rightMonkey: BetterMonkey? = null, var result: Long? = null) {
    var isHuman: Boolean = false

    init {
        if (id == "humn") { isHuman = true }
    }

    override fun toString(): String {
        return if (result != null) {
            if (operator != null) {
                "$id: ${leftMonkey?.id} $operator ${rightMonkey?.id} (= $result)"
            } else {
                "$id: $result"
            }
        } else {
            "$id: ${leftMonkey?.id} $operator ${rightMonkey?.id} (= ????)"
        }
    }

    fun evaluate(left: Long, right: Long): Long {
        return when (operator) {
            "+" -> left + right
            "-" -> left - right
            "/" -> left / right
            "*" -> left * right
            else -> {
                throw Exception("Unknown operator: $operator")
            }
        }
    }

    fun inverse(result: Long, operand: Long, isLeft: Boolean): Long {
        return when (operator) {
            "+" -> result - operand
            "-" -> if (isLeft) result + operand else operand - result
            "/" -> if (isLeft) result * operand else operand / result
            "*" -> result / operand
            else -> {
                throw Exception("Unknown operator: $operator")
            }
        }
    }
}

fun main() {
    fun parseMonkeys(inputs: List<String>, part2: Boolean = false): Map<String, BetterMonkey> {
        val monkeys = mutableMapOf<String, BetterMonkey>()

        val smartMonkeyRegex = "^(\\w+): (\\w+) ([+*/-]) (\\w+)$".toRegex()
        val dumbMonkeyRegex = "^(\\w+): (-?\\d+)$".toRegex()

        fun fromSmartMonkey(it: String): BetterMonkey? {
            if (!smartMonkeyRegex.matches(it)) { return null }
            val match = smartMonkeyRegex.matchEntire(it)
            var (id, left, op, right) = match!!.destructured
            if (part2 && id == "root") { op = "=" }
            return monkeys.getOrPut(id) {
                BetterMonkey(id, operator = op)
            }.also {
                it.leftMonkey = monkeys.getOrPut(left) {
                    BetterMonkey(left)
                }

                it.rightMonkey = monkeys.getOrPut(right) {
                    BetterMonkey(right)
                }

                it.operator = op
            }
        }

        fun fromDumbMonkey(it: String): BetterMonkey {
            val match = dumbMonkeyRegex.matchEntire(it)
            val (id, yelled) = match!!.destructured

            return monkeys.getOrPut(id) {
                BetterMonkey(id, result = yelled.toLong())
            }.also {
                it.result = yelled.toLong()

                if (part2 && it.isHuman) {
                    it.result = null
                }

                it.leftMonkey = null
                it.rightMonkey = null
            }
        }

        inputs.forEach {
            fromSmartMonkey(it) ?: fromDumbMonkey(it)
        }

        return monkeys
    }

    fun solvePart1(monkey: BetterMonkey, monkeys: Map<String, BetterMonkey>): Long? {
        return if (monkey.result != null) {
            monkey.result!!
        } else {
            if (monkey.operator == null) {
                return null
            }

            val left = solvePart1(monkey.leftMonkey!!, monkeys)
            val right = solvePart1(monkey.rightMonkey!!, monkeys)

            if (left != null && right != null) {
                monkey.result = monkey.evaluate(left, right)
            }

            monkey.result
        }
    }

    fun solvePart2(monkey: BetterMonkey, monkeys: Map<String, BetterMonkey>, result: Long): Long {
        monkey.result = result
        return if (monkey.id == "humn") {
            result
        } else {
            if (monkey.leftMonkey!!.result == null) {
                solvePart2(monkey.leftMonkey!!, monkeys, monkey.inverse(result, monkey.rightMonkey!!.result!!, isLeft = true))
            } else {
                solvePart2(monkey.rightMonkey!!, monkeys, monkey.inverse(result, monkey.leftMonkey!!.result!!, isLeft = false))
            }
        }
    }

    fun part1(input: List<String>): Long {
        val monkeys = parseMonkeys(input)
        return solvePart1(monkeys["root"]!!, monkeys)!!
    }

    fun part2(input: List<String>): Long {
        val monkeys = parseMonkeys(input, true)
        val root = monkeys["root"]!!
        solvePart1(root, monkeys)

        return if (root.leftMonkey!!.result == null) {
            solvePart2(root.leftMonkey!!, monkeys, root.rightMonkey!!.result!!)
        } else {
            solvePart2(root.rightMonkey!!, monkeys, root.leftMonkey!!.result!!)
        }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day21_test")
    check(part1(testInput) == 152L)
    println("Part 1 checked out!")
    check(part2(testInput) == 301L)
    println("Part 2 checked out!")

    val input = readInput("Day21")
    println(part1(input))
    println(part2(input))
}
