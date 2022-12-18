// drawn in reverse (bottom row first)
val SHAPES = listOf(
    listOf(0xF0).reversed(),
    listOf(0X40, 0xE0, 0x40).reversed(),
    listOf(0x20, 0x20, 0xE0).reversed(),
    listOf(0x80, 0x80, 0x80, 0x80).reversed(),
    listOf(0xC0 ,0xC0).reversed()
)

val SHAPE_WIDTHS = listOf(4, 3, 3, 1, 2)
val SHAPE_HEIGHTS = listOf(1, 3, 3, 4, 2)

class Tetris() {
    var shapeIndex = -1
    var shapeWidth = -1
    var shapeHeight = -1
    var shapeX = -1
    var shapeY = -1
    var shape = listOf<Int>()
    var grid = mutableListOf<Int>() // 0 == empty row, index 0 == bottom row
    var shapeCount = 0L
    var rowsCulled = 0L

    init { nextShape() }

    fun rockHeight(): Int {
        for (y in grid.lastIndex downTo 0) {
            if (grid[y] > 0) return y + 1
        }
        return 0
    }

    fun totalRockHeight(): Long {
        return rowsCulled + rockHeight()
    }

    fun nextShape() {
        shapeIndex = ++shapeIndex % SHAPES.size
        shapeWidth = SHAPE_WIDTHS[shapeIndex]
        shapeHeight = SHAPE_HEIGHTS[shapeIndex]
        shapeX = 2
        shapeY = 3 + rockHeight()
        shape = SHAPES[shapeIndex]
        shapeCount++
        while (grid.size < shapeY + shapeHeight) {
            grid.add(0)
        }
    }

    // returns true if the current shape collides with the grid
    // the transformShape function transforms each byte of the shape
    // the transformGridY function transforms the y-coordinate of the grid that we target
    fun collides(transformShape: (Int) -> Int = fun(b: Int) = b, transformGridY: (Int) -> Int = fun(y: Int) = y ): Boolean {
        // for each row in the shape, check if it collides with the row given by transformGridY
        for (y in shapeY until shapeY + shapeHeight) {
            val target = grid[transformGridY(y)]
            val transformed = transformShape(shape[y - shapeY]) shr shapeX // put it in the right x-coordinate
            val expected = transformed or target
            val collided = transformed xor target

            if (collided != expected) {
                return true
            }
        }

        return false
    }

    fun fall(): Boolean {
        if (shapeY == 0 || collides(transformGridY = { y -> y -1 })) {
            blit() // no more moving this shape, and fix it into the grid permanently
            return true
        }

        shapeY--

        return false
    }

    fun blow(direction: Char) {
        when(direction) {
            '<' -> {
                if (shapeX == 0) { return }
                if (collides({ b: Int -> b shl 1 })) { return }
                shapeX--
            }

            '>' -> {
                if (shapeX + shapeWidth - 1 == 6) { return }
                if (collides({ b: Int -> b shr 1 })) { return }
                shapeX++
            }
        }
    }

    // copy the shape into the grid
    fun blit() {
        for (y in shapeY until shapeY + shapeHeight) {
            val row = (shape[y - shapeY] shr shapeX) or grid[y]
            grid[y] = row
        }

        // determine if we can cull rows from the grid, working down from the top
        // find the index of the first row where we've seen every column filled
        var seen = 0
        var rowIndexToCull = -1
        for (y in grid.lastIndex downTo 0) {
            seen = seen or grid[y]
            if (seen == 0xFE) {
                rowIndexToCull = y
                break
            }
        }

        if (rowIndexToCull > -1) {
            rowsCulled += rowIndexToCull
            grid = grid.drop(rowIndexToCull).filter { it > 0 }.toMutableList()
        } else {
            grid = grid.filter { it > 0 }.toMutableList()
        }
    }

    fun draw(drawShape: Boolean = true) {
        val shapeCopy = shape.map { it shr shapeX }

        for (y in grid.lastIndex downTo 0) {
            print("|")
            for (x in 0..6) {
                val xBit = 0x80 shr x
                if (drawShape && (y in shapeY until shapeY+shapeHeight)) {
                    if ((shapeCopy[y - shapeY] and xBit) != 0) {
                        print("@")
                        continue
                    }
                }

                if (grid[y] and xBit != 0) {
                    print("#")
                } else {
                    print(".")
                }
            }
            print("|")
            print("\n")
        }
        println("+-------+")
        println("")
    }

    fun advanceCycle(cycleRockHeight: Long, cycleShapeCount: Long) {
        rowsCulled += cycleRockHeight
        shapeCount += cycleShapeCount
    }
}

fun main() {
    fun part1(input: List<String>, shapeCountLimit: Long = 2022L): Long {
        val jetStream = input.first().toList()
        var jetStreamIndex = 0
        val tetris = Tetris()
        var isBlowing = true

        val cycleDetection = mutableMapOf<Triple<Int, Int, Int>,Pair<Long,Long>>()
        var cycleDetected = false

        while (tetris.shapeCount <= shapeCountLimit) {
            if (isBlowing) {
                tetris.blow(jetStream[jetStreamIndex])
                jetStreamIndex = ++jetStreamIndex % jetStream.size
            } else {
                if (tetris.fall()) {
                    if (tetris.shapeCount > 2022L) {
                        // this block has stuck, record the jetStreamIndex, shapeIndex, and hash of the grid layout and map it to the (rock height, shape count) at this point in time
                        val hashKey = Triple(jetStreamIndex, tetris.shapeIndex, tetris.grid.hashCode())
                        val hashVal = Pair(tetris.totalRockHeight(), tetris.shapeCount)

                        if (cycleDetection.containsKey(hashKey)) { // we've reached another jetStream, shapeIndex, gridLayout that matches one we saw before...
                            val cycleDelta = Pair(hashVal.first - cycleDetection[hashKey]!!.first, hashVal.second - cycleDetection[hashKey]!!.second)
                            println("Cycle at $hashKey; $cycleDelta")
                            cycleDetection[hashKey] = hashVal
                            // tetris.draw(false)
                            val shapesRemaining = shapeCountLimit - tetris.shapeCount
                            val cyclesInShapesRemaining = shapesRemaining / cycleDelta.second
                            tetris.advanceCycle(cycleDelta.first * cyclesInShapesRemaining, cycleDelta.second * cyclesInShapesRemaining)
                        } else if (!cycleDetected) {
                            cycleDetection[hashKey] = Pair(tetris.totalRockHeight(), tetris.shapeCount)
                            cycleDetected = true
                        }
                    }
                    tetris.nextShape()
                }
            }
            isBlowing = !isBlowing
        }

        return tetris.totalRockHeight()
    }


    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day17_test")
    check(part1(testInput, 2022L) == 3068L)
    println("Part 1 checked out ok!")
    check(part1(testInput, 1000000000000L) == 1514285714288L)
    println("Part 2 checked out ok!")

    val input = readInput("Day17")
    println(part1(input, 2022L))
    println(part1(input, 1000000000000L))
}
