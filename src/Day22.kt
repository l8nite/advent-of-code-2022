enum class Facing(val value: Int, val char: Char) {
    RIGHT(0, '>'),
    DOWN(1, 'v'),
    LEFT(2, '<'),
    UP(3, '^')
}

class MapData(val range: IntRange, val chars: List<Char>) {}

class Walker(
    val rowMap: List<MapData>,
    val colMap: List<MapData>,
    var absoluteX: Int = 0,
    var absoluteY: Int = 0,
    var relativeX: Int = 0,
    var relativeY: Int = 0,
    var facing: Facing = Facing.RIGHT,
    val faceWidth: Int,
    val part2: Boolean
) {
    private val history = mutableMapOf<Pair<Int, Int>, Facing>() // stores the last facing at each x,y coordinate we visit
    private val mapWidth = rowMap.maxOf { it.range.last } + 1

    init {
        relativeX = rowMap[absoluteY].chars.indexOfFirst { it == '.' }
        absoluteX = relativeX + rowMap[absoluteY].range.first
        record()
    }

    private fun record() {
        history[Pair(absoluteX, absoluteY)] = facing
    }

    fun drawMap() {
        rowMap.forEachIndexed { y, row ->
            for (x in 0 .. mapWidth) {
                if (x < row.range.first) { print(" ") }
                else if (x in row.range) {
                    if (x == absoluteX && y == absoluteY) { print(facing.char) }
                    else { print(history[Pair(x, y)]?.char ?: row.chars[x - row.range.first]) }
                } else if (x > row.range.last) { print(" ") }
            }
            println()
        }
    }

    fun updateFacing(rotation: Char) {
        facing = when (rotation) {
            'L' -> when (facing) {
                Facing.LEFT -> Facing.DOWN
                Facing.DOWN -> Facing.RIGHT
                Facing.RIGHT -> Facing.UP
                Facing.UP -> Facing.LEFT
            }
            'R' -> when (facing) {
                Facing.LEFT -> Facing.UP
                Facing.UP -> Facing.RIGHT
                Facing.RIGHT -> Facing.DOWN
                Facing.DOWN -> Facing.LEFT
            }
            else -> TODO()
        }
        record()
    }

    override fun toString(): String {
        return "Absolute: ($absoluteX, $absoluteY), Relative: ($relativeX, $relativeY), Facing: $facing"
    }

    private fun moveX(n: Int) {
        val maxX = rowMap[absoluteY].chars.lastIndex
        var newX = relativeX + n

        // wrap around
        if (newX < 0) { newX = maxX }
        else if (newX > maxX) { newX = 0 }

        if (rowMap[absoluteY].chars[newX] == '#') { return }

        relativeX = newX
        absoluteX = newX + rowMap[absoluteY].range.first
        relativeY = (absoluteY - colMap[absoluteX].range.first) // we changed columns
    }

    private fun moveY(n: Int) {
        val maxY = colMap[absoluteX].chars.lastIndex
        var newY = relativeY + n

        // wrap around
        if (newY < 0) { newY = maxY }
        else if (newY > maxY) { newY = 0 }

        if (colMap[absoluteX].chars[newY] == '#') { return }

        relativeY = newY
        absoluteY = newY + colMap[absoluteX].range.first
        relativeX = (absoluteX - rowMap[absoluteY].range.first) // we changed rows
    }

    private fun moveXCubeFoldTest(n: Int) {
        val maxX = rowMap[absoluteY].chars.lastIndex
        val oldRelativeX = relativeX
        val oldRelativeY = relativeY
        val oldAbsoluteX = absoluteX
        val oldAbsoluteY = absoluteY
        val oldFacing = facing
        val newX = relativeX + n

        if (newX < 0) { // hit left edge of the cube face
            when (absoluteY) {
                in 0 until 4 -> { // face1 left connects to face3 top
                    absoluteX = 4 + (relativeY - 0)
                    absoluteY = 4
                    relativeX = (absoluteX - rowMap[absoluteY].range.first)
                    relativeY = 0
                    facing = Facing.DOWN
                }
                in 4 until 8 -> { // face2 left connects to face6 bottom
                    absoluteX = 12 + (3 - (relativeY - 0))
                    absoluteY = 15
                    relativeX = (3 - (relativeY - 0))
                    relativeY = 3
                    facing = Facing.UP
                }
                in 8 until 12 -> { // face5 left connects to face3 bottom
                    absoluteX = 4 + (3 - (relativeY - 8))
                    absoluteY = 7
                    relativeX = (3 - (relativeY - 8))
                    relativeY = 3
                    facing = Facing.UP
                }
            }
        } else if (newX > maxX) {
            when (absoluteY) {
                in 0 until 4 -> { // face1 right connects to 6 right
                    absoluteX = 15
                    absoluteY = 8 + (3 - (relativeY - 0))
                    relativeX = 3
                    relativeY = (3 - (relativeY - 0))
                    facing = Facing.LEFT
                }
                in 4 until 8 -> { // face4 right connects to face6 top
                    absoluteX = 12 + (3 - (relativeY - 4))
                    absoluteY = 8
                    relativeX = (3 - (relativeY - 4))
                    relativeY = 0
                    facing = Facing.DOWN
                }
                in 8 until 12 -> { // face6 right connects to face1 right
                    absoluteX = 4 + (3 - (relativeY - 0))
                    absoluteY = 7
                    relativeX = (3 - (relativeY - 0))
                    relativeY = 3
                    facing = Facing.LEFT
                }
            }
        } else {
            relativeX = newX
            absoluteX = newX + rowMap[absoluteY].range.first
            relativeY = (absoluteY - colMap[absoluteX].range.first) // we changed columns
        }

        if (rowMap[absoluteY].chars[relativeX] == '#') {
            absoluteX = oldAbsoluteX
            absoluteY = oldAbsoluteY
            relativeX = oldRelativeX
            relativeY = oldRelativeY
            facing = oldFacing
            return
        }
    }

    private fun moveYCubeFoldTest(n: Int) {
        val maxY = colMap[absoluteX].chars.lastIndex
        val oldRelativeX = relativeX
        val oldRelativeY = relativeY
        val oldAbsoluteX = absoluteX
        val oldAbsoluteY = absoluteY
        val oldFacing = facing
        val newY = relativeY + n

        if (newY < 0) { // hit top edge of the cube face
            when (absoluteX) {
                in 0 until 4 -> { // face2 top connects to face1 top
                    absoluteY = 0
                    absoluteX = 8 + (3 - (relativeX - 0))
                    relativeY = 0
                    relativeX = (3 - (relativeX - 0))
                    facing = Facing.DOWN
                }
                in 4 until 8 -> { // face3 top connects to face1 left
                    absoluteY = 0 + (relativeX - 4)
                    absoluteX = 8
                    relativeY = (relativeX - 4)
                    relativeX = 0
                    facing = Facing.RIGHT
                }
                in 8 until 12 -> { // face1 top connects to face2 top
                    absoluteY = 4
                    absoluteX = (3 - (relativeX - 0))
                    relativeY = 0
                    relativeX = (3 - (relativeX - 0))
                    facing = Facing.DOWN
                }
                in 12 until 16 -> { // face6 top connects to face4 right
                    absoluteY = 4 + (3 - (relativeX - 4))
                    absoluteX = 11
                    relativeY = (3 - (relativeX - 4))
                    relativeX = 3
                    facing = Facing.LEFT
                }
            }
        } else if (newY > maxY) { // moved off the bottom edge of a cube face
            when (absoluteX) {
                in 0 until 4 -> { // face2 bottom connects to face5 bottom
                    absoluteY = 11
                    absoluteX = 8 + (3 - (relativeX - 0))
                    relativeY = 3
                    relativeX = (3 - (relativeX - 0))
                    facing = Facing.UP
                }
                in 4 until 8 -> { // face3 bottom connects to face5 left
                    absoluteY = 8 + (relativeX - 4)
                    absoluteX = 8
                    relativeY = (3 - (relativeX - 4))
                    relativeX = 0
                    facing = Facing.RIGHT
                }
                in 8 until 12 -> { // face5 bottom connects to face2 bottom
                    absoluteY = 7
                    absoluteX = (3 - (relativeX - 0))
                    relativeY = 3
                    relativeX = (3 - (relativeX - 0))
                    facing = Facing.UP
                }
                in 12 until 16 -> { // face6 bottom connects to face2 left
                    absoluteY = 4 + (3 - (relativeX - 4))
                    absoluteX = 0
                    relativeY = (3 - (relativeX - 4))
                    relativeX = 0
                    facing = Facing.RIGHT
                }
            }
        } else { // we didn't hit an edge
            relativeY = newY
            absoluteY = newY + colMap[absoluteX].range.first
            relativeX = (absoluteX - rowMap[absoluteY].range.first)
        }

        if (colMap[absoluteX].chars[relativeY] == '#') {
            absoluteX = oldAbsoluteX
            absoluteY = oldAbsoluteY
            relativeX = oldRelativeX
            relativeY = oldRelativeY
            facing = oldFacing
            return
        }
    }

    private fun moveXCubeFoldReal(n: Int) {
        val maxX = rowMap[absoluteY].chars.lastIndex
        val oldRelativeX = relativeX
        val oldRelativeY = relativeY
        val oldAbsoluteX = absoluteX
        val oldAbsoluteY = absoluteY
        val oldFacing = facing
        val newX = relativeX + n

        if (newX < 0) { // hit left edge of the cube face
            when (absoluteY) {
                in 0 until 50 -> { // face1 left connects to face3 left
                    absoluteX = 0
                    absoluteY = 100 + (49 - relativeY)
                    relativeX = 0
                    relativeY = (49 - relativeY)
                    facing = Facing.RIGHT
                }
                in 50 until 100 -> { // face2 left connects to face3 top
                    absoluteX = 0 + (relativeY - 50)
                    absoluteY = 100
                    relativeX = (relativeY - 50)
                    relativeY = 0
                    facing = Facing.DOWN
                }
                in 100 until 150 -> { // face3 left connects to face1 left
                    absoluteX = 50
                    absoluteY = 0 + (49 - relativeY)
                    relativeX = 0
                    relativeY = (49 - relativeY)
                    facing = Facing.RIGHT
                }
                in 150 until 200 -> { // face5 left connects to face1 top
                    absoluteX = 0 + (relativeY)
                    absoluteY = 0
                    relativeX = 0 + (relativeY - 50)
                    relativeY = 0
                    facing = Facing.DOWN
                }
            }
        } else if (newX > maxX) {
            when (absoluteY) {
                in 0 until 50 -> { // face4 right connects to face6 right
                    absoluteX = 99
                    absoluteY = 100 + (49 - relativeY)
                    relativeX = 99
                    relativeY = 100 + (49 - relativeY)
                    facing = Facing.LEFT
                }
                in 50 until 100 -> { // face2 right connects to face4 bottom
                    absoluteX = 100 + (relativeY - 50)
                    absoluteY = 49
                    relativeX = 50 + (relativeY - 50)
                    relativeY = 49
                    facing = Facing.UP
                }
                in 100 until 150 -> { // face6 right connects to face4 right
                    absoluteX = 149
                    absoluteY = (49 - (relativeY - 100))
                    relativeX = 99
                    relativeY = (49 - (relativeY - 100))
                    facing = Facing.LEFT
                }
                in 150 until 200 -> { // face5 right connects to face6 bottom
                    absoluteX = 50 + (relativeY - 50)
                    absoluteY = 149
                    relativeX = 50 + (relativeY - 50)
                    relativeY = 149
                    facing = Facing.UP
                }
            }
        } else {
            relativeX = newX
            absoluteX = newX + rowMap[absoluteY].range.first
            relativeY = (absoluteY - colMap[absoluteX].range.first) // we changed columns
        }

        if (rowMap[absoluteY].chars[relativeX] == '#') {
            absoluteX = oldAbsoluteX
            absoluteY = oldAbsoluteY
            relativeX = oldRelativeX
            relativeY = oldRelativeY
            facing = oldFacing
            return
        }
    }

    private fun moveYCubeFoldReal(n: Int) {
        val maxY = colMap[absoluteX].chars.lastIndex
        val oldRelativeX = relativeX
        val oldRelativeY = relativeY
        val oldAbsoluteX = absoluteX
        val oldAbsoluteY = absoluteY
        val oldFacing = facing
        val newY = relativeY + n

        if (newY < 0) { // hit top edge of the cube face
            when (absoluteX) {
                in 0 until 50 -> { // face3 top connects to face2 left
                    absoluteY = 50 + (relativeX)
                    absoluteX = 50
                    relativeY = 50 + (relativeX)
                    relativeX = 0
                    facing = Facing.RIGHT
                }
                in 50 until 100 -> { // face1 top connects to face5 left
                    absoluteY = 150 + (relativeX)
                    absoluteX = 0
                    relativeY = 50 + (relativeX)
                    relativeX = 0
                    facing = Facing.RIGHT
                }
                in 100 until 150 -> { // face4 top connects to face5 bottom
                    absoluteY = 199
                    absoluteX = 0 + (relativeX - 50)
                    relativeY = 99
                    relativeX = 0 + (relativeX - 50)
                    facing = Facing.UP
                }
            }
        } else if (newY > maxY) { // moved off the bottom edge of a cube face
            when (absoluteX) {
                in 0 until 50 -> { // face5 bottom connects to face4 top
                    absoluteY = 0
                    absoluteX = 100 + (relativeX)
                    relativeY = 0
                    relativeX = 0 + (50 + (relativeX))
                    facing = Facing.DOWN
                }
                in 50 until 100 -> { // face6 bottom connects to face5 right
                    absoluteY = 150 + (relativeX - 50)
                    absoluteX = 49
                    relativeY = 50 + (relativeX - 50)
                    relativeX = 49
                    facing = Facing.LEFT
                }
                in 100 until 150 -> { // face4 bottom connects to face2 right
                    absoluteY = 50 + (relativeX - 50)
                    absoluteX = 99
                    relativeY = 50 + (relativeX - 50)
                    relativeX = 49
                    facing = Facing.LEFT
                }
            }
        } else { // we didn't hit an edge
            relativeY = newY
            absoluteY = newY + colMap[absoluteX].range.first
            relativeX = (absoluteX - rowMap[absoluteY].range.first)
        }

        if (colMap[absoluteX].chars[relativeY] == '#') {
            absoluteX = oldAbsoluteX
            absoluteY = oldAbsoluteY
            relativeX = oldRelativeX
            relativeY = oldRelativeY
            facing = oldFacing
            return
        }
    }

    fun move() {
        if (part2 && faceWidth == 4) {
            when (facing) {
                Facing.RIGHT -> moveXCubeFoldTest(1)
                Facing.LEFT -> moveXCubeFoldTest(-1)
                Facing.DOWN -> moveYCubeFoldTest(1)
                Facing.UP -> moveYCubeFoldTest(-1)
            }
        } else if (part2 && faceWidth == 50) {
            when (facing) {
                Facing.RIGHT -> moveXCubeFoldReal(1)
                Facing.LEFT -> moveXCubeFoldReal(-1)
                Facing.DOWN -> moveYCubeFoldReal(1)
                Facing.UP -> moveYCubeFoldReal(-1)
            }
        } else {
            when (facing) {
                Facing.RIGHT -> moveX(1)
                Facing.LEFT -> moveX(-1)
                Facing.DOWN -> moveY(1)
                Facing.UP -> moveY(-1)
            }
        }
        record()
    }
}

fun main() {
    fun parse2d(input: List<String>): List<MapData> {
        val mapRegex = "^ *[ .#]+ *$".toRegex()
        val rowMap = mutableListOf<MapData>()

        input.takeWhile { mapRegex.matches(it) }.forEach {
            val start = it.indexOfAny(listOf('.', '#').toCharArray())
            var end = it.indexOf(' ', start)
            if (end == -1) {
                end = it.lastIndex
            }
            val range = start..end
            val text = it.substring(range)
            rowMap.add(MapData(range, text.toList()))
        }

        return rowMap
    }

    fun invert2d(rowMap: List<MapData>): List<MapData> {
        val colMap = mutableListOf<MapData>()
        (0 until rowMap.maxOf { it.range.last } + 1).forEach { colIdx ->
            val rangeStart = rowMap.indexOfFirst { colIdx in it.range }
            val rangeEnd = rowMap.indexOfLast { colIdx in it.range }
            val chars = rowMap.filter { colIdx in it.range }.map { it.chars[colIdx - it.range.first] }
            colMap.add(MapData(
                rangeStart..rangeEnd,
                chars
            ))
        }

        return colMap
    }

    fun walk2d(input: List<String>, faceWidth: Int = 4, part2: Boolean = false): Int {
        val rowMap = parse2d(input)
        val colMap = invert2d(rowMap)

        val walker = Walker(rowMap, colMap, faceWidth = faceWidth, part2 = part2)

        val directions = input.drop(rowMap.size + 1).first().split("(?<=[LR])".toRegex())
        directions.forEach { direction ->
            val moveCount = direction.dropLast(if (direction.last() in listOf('L', 'R')) { 1 } else { 0 }).toInt()

            // println("$walker -> Moving $moveCount steps...")

            repeat(moveCount) {
                walker.move()
            }

            // change facing
            val d = direction.last()
            if (d in listOf('L', 'R')) {
                walker.updateFacing(d)
                // println("Rotating $direction, new facing is: ${walker.facing}")
            }

            // walker.drawMap()
        }

        // println("\n=== AFTER TRAVELING===\n")
        // walker.drawMap()

        return (1000 * (walker.absoluteY+1) + 4 * (walker.absoluteX+1) + walker.facing.value)
    }

    fun part1(input: List<String>): Int {
        return walk2d(input)
    }

    fun part2(input: List<String>, width: Int): Int {
        return walk2d(input, width, part2 = true)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day22_test")
    check(part1(testInput) == 6032)
    println("Part 1 checked out!")
    check(part2(testInput, 4) == 5031)
    println("Part 2 checked out!")

    val input = readInput("Day22")
    println(part1(input))
    println(part2(input, 50))
}
