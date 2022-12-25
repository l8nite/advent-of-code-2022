import kotlin.math.abs
import kotlin.math.pow

infix fun Long.`**`(exponent: Int): Long = toDouble().pow(exponent).toLong()

val powersOf5 = (0..20).map {
    5L `**` it
}

val powersOf5Max = (0..20).map {
    (0..it).sumOf { p -> 2 * powersOf5[p] }
}

fun main() {
    fun parseSnafuToDecimal(snafu: String): Long {
        return snafu.reversed().mapIndexed { i, it ->
            if (it.isDigit()) {
                it.digitToInt() * powersOf5[i]
            } else {
                when (it) {
                    '=' -> -2 * powersOf5[i]
                    '-' -> -1 * powersOf5[i]
                    else -> throw Exception("Unknown SNAFU digit")
                }
            }
        }.sum()
    }

    fun convertDecimalToSnafu(n: Long): String {
        val digits = mutableListOf<Char>()
        var remaining = abs(n) // todo; do we need to worry about negatives?

        // var powerIndex = powersOf5.indexOfFirst { n <= 2*it }

        // 1=-0-2 = 1747
        // 3,125 + (-2 * 625) + (-1 * 125) + (0) + (-1 * 5) + 2
        for (powerIndex in powersOf5.lastIndex downTo 1) {
            val currentPower = powersOf5[powerIndex]
            val currentPowerTwoMax = powersOf5Max[powerIndex]
            val nextLowerPowerMax = powersOf5Max[powerIndex-1]
            val currentPowerOneMax = nextLowerPowerMax + currentPower


            if (remaining == 0L) {
                digits.add('0')
                continue
            }

            // current value is too high, because we had to go up a power
            if (remaining < 0L) {
                // if what we have left fits in the next lower power, add a 0 here
                if (abs(remaining) <= nextLowerPowerMax) {
                    digits.add('0')
                    continue
                }

                if (abs(remaining) <= currentPowerOneMax) {
                    digits.add('-')
                    remaining += currentPower
                    continue
                }

                if (abs(remaining) <= currentPowerTwoMax) {
                    digits.add('=')
                    remaining += currentPower * 2
                    continue
                }
            }

            // if what we have left fits in the next lower power, add a 0 here
            if (remaining <= nextLowerPowerMax) {
                digits.add('0')
                continue
            }

            if (remaining <= currentPowerOneMax) { // but it fits in 1x currentPower
                digits.add('1')
                remaining -= currentPower
                continue
            }

            if (remaining <= currentPowerTwoMax) { // it must fit in 2x currentPower
                digits.add('2')
                remaining -= currentPower * 2
                continue
            }
        }

        when (remaining) {
            2L -> { digits.add('2') }
            1L -> { digits.add('1') }
            0L -> { digits.add('0') }
            -1L -> { digits.add('-') }
            -2L -> { digits.add('=') }
            else -> throw Exception("BUG: I got a remainder of $remaining")
        }

        val firstNonZero = digits.indexOfFirst { it != '0' }

        return digits.drop(if (firstNonZero == -1) 0 else firstNonZero).joinToString("")
    }

    fun part1(input: List<String>): Long {
        listOf(1,2,3,4,5,6,7,8,9,10, 15, 20, 2022, 12345, 314159265).forEach {
            print("$it = ")
            print(convertDecimalToSnafu(it.toLong()))
            println()
        }

        val sum = input.sumOf {
            parseSnafuToDecimal(it)
        }

        println("Answer for $sum is ${convertDecimalToSnafu(sum)}")

        return sum
    }

//    fun part2(input: List<String>): Long {
//        return input.size.toLong()
//    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day25_test")
    check(part1(testInput) == 4890L)
    println("Part 1 checked out!")
//    check(part2(testInput) == 1L)
//    println("Part 2 checked out!")

    val input = readInput("Day25")
    println(part1(input))
//    println(part2(input))
}
