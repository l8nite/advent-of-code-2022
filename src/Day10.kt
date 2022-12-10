enum class Command(val cycles: Int) {
    addx(2),
    noop(1)
}

class Instruction(var cmd: Command, var arg: Int? = null) {
}

class Computer(private val instructions: List<Instruction>, var x: Int = 1) {
    private var cc = 0
    private var ip = 0
    val screen = Screen(40, 6)
    private var cycle = 0

    fun tick() {
        if (cc == instructions[ip].cmd.cycles) {
            when (instructions[ip].cmd) {
                Command.addx -> x += instructions[ip].arg!!
                else -> null
            }
            ip++
            cc = 0
        }
        screen.draw(cycle++, x)
        cc++
    }
}

class Screen(private val width: Int, height: Int) {
    private val size = width * height
    private val display = MutableList(size) { ' ' }
    fun draw(cycle: Int, x: Int) {
        val idx = cycle % size
        val row = cycle / width
        if ((idx-1..idx+1).contains(x + row * width)) {
            display[cycle] = '#'
        } else {
            display[cycle] = ' '
        }
    }

    fun print() {
        display.chunked(width).forEach {
            println(it.joinToString(""))
        }
    }
}

fun main() {
    fun parse(input: List<String>): List<Instruction> = input.map { line ->
        line.split(" ").let {
            Instruction(Command.valueOf(it[0]), it.getOrNull(1)?.toInt())
        }
    }

    fun part1(input: List<String>): Int {
        val computer = Computer(parse(input))
        var acc = 0
        val samples = listOf(20, 60, 100, 140, 180, 220)

        (1..220).forEach {
            computer.tick()
            if (samples.contains(it)) acc += it * computer.x
        }

        return acc
    }

    fun part2(input: List<String>) {
        val computer = Computer(parse(input))

        (1..240).forEach {
            computer.tick()
        }

        computer.screen.print()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day10_test")
    check(part1(testInput) == 13140)

    val input = readInput("Day10")
    println(part1(input))
    part2(input)
}
