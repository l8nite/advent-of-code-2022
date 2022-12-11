import java.math.BigInteger

class Monkey(
    private val items: MutableList<BigInteger>,
    val operation: ((BigInteger) -> BigInteger),
    val divisor: BigInteger,
    private val trueTarget: Int,
    private val falseTarget: Int,
    var worry: ((BigInteger) -> (BigInteger))? = null
) {
    var inspected: BigInteger = BigInteger.ZERO

    fun inspect(monkeys: List<Monkey>) {
        items.forEach {
            var item = operation(it)

            item = worry?.invoke(item) ?: item

            if (item % divisor == BigInteger.ZERO) {
                monkeys[trueTarget].items.add(item)
            } else {
                monkeys[falseTarget].items.add(item)
            }
        }

        inspected += items.size.toBigInteger()

        items.clear()
    }

//    fun report(idx: Int) {
//        println("Monkey $idx:")
//        println("  Items: $items")
//        println("  Divisor: $divisor")
//        println("  Worry: $worry")
//        println("  Targets: [$trueTarget, $falseTarget]")
//        println("  Inspected: $inspected")
//    }
}

fun parseMonkey(input: List<String>): Monkey {
    val items = parseItems(input[1])
    val operation = parseOperation(input[2])
    val divisor = parseTestDivisor(input[3])
    val (trueTarget, falseTarget) = parseTrueFalseTargets(input[4], input[5])

    return Monkey(
        items,
        operation,
        divisor,
        trueTarget,
        falseTarget
    ) { n -> n / BigInteger.valueOf(3) }
}

fun parseItems(input: String): MutableList<BigInteger> {
    val itemsRegex = "^\\s*Starting items: (.+)+$".toRegex()
    val (itemString) = itemsRegex.matchEntire(input)!!.destructured
    return itemString.split(", ".toRegex()).map(String::toBigInteger).toMutableList()
}

fun parseOperation(input: String): ((BigInteger) -> BigInteger) {
    val operationRegex = "^\\s*Operation: new = (.+?) ([*+]) (.+?)$".toRegex()
    val (l, o, r) = operationRegex.matchEntire(input)!!.destructured

    return fun (n: BigInteger): BigInteger  {
        val left = if (l == "old") n else l.toBigInteger()
        val right = if (r == "old") n else r.toBigInteger()

        return when (o) {
            "+" -> left + right
            "*" -> left * right
            else -> throw Exception("Unknown operation: $o")
        }
    }
}

fun parseTestDivisor(input: String): BigInteger {
    val testRegex = "^\\s*Test: divisible by (\\d+)$".toRegex()
    val (divisor) = testRegex.matchEntire(input)!!.destructured
    return divisor.toBigInteger()
}

fun parseTrueFalseTargets(trueInput: String, falseInput: String): Pair<Int,Int> {
    val throwTargetRegex = "^\\s*If (?:true|false): throw to monkey (\\d+)$".toRegex()
    val (trueTarget) = throwTargetRegex.matchEntire(trueInput)!!.destructured
    val (falseTarget) = throwTargetRegex.matchEntire(falseInput)!!.destructured
    return Pair(trueTarget.toInt(), falseTarget.toInt())
}

fun main() {
    fun part1(input: List<String>): BigInteger {
        val monkeys = input.chunked(7).map { parseMonkey(it) }

        repeat(20) {
            monkeys.forEach { it.inspect(monkeys) }
        }

        val top2 = monkeys.map { it.inspected }.sortedDescending().take(2)
        return top2[0] * top2[1]
    }

    fun part2(input: List<String>): BigInteger {
        val monkeys = input.chunked(7).map { parseMonkey(it) }
        val lcm = monkeys.map { it.divisor }.reduce { acc, n -> acc * n }
        val function = { n: BigInteger -> n % lcm }
        monkeys.forEach { it.worry = function }

        repeat(10000) {
            monkeys.forEach { it.inspect(monkeys) }
        }

        val top2 = monkeys.map { it.inspected }.sortedDescending().take(2)
        return top2[0] * top2[1]
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day11_test")
    check(part1(testInput) == BigInteger.valueOf(10605))
    check(part2(testInput) == BigInteger.valueOf(2713310158L))

    val input = readInput("Day11")
    println(part1(input))
    println(part2(input))
}
