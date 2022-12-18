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

    fun fall(): Boolean { // returns the count of shapes on grid (including the new one just generated)
        if (shapeY == 0 || collides(transformGridY = { y -> y -1 })) {
            blit() // stop moving this shape and move on to the next one
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

    fun advanceCycle(cycleShapeCount: Long, cycleRockHeight: Long) {
        // if this is called, it means that the current grid has "repeated the cycle", meaning the current map
        // is exactly what we'd get every time we iterated another time through the cycle...
        shapeCount += cycleShapeCount
        rowsCulled += cycleRockHeight
    }
}

fun main() {
    fun part1(input: List<String>, shapeCountLimit: Long = 2022L): Long {
        val jetStream = input.first().toList()
        var jetStreamIndex = 0
        val tetris = Tetris()
        var isBlowing = true

        while (tetris.shapeCount <= shapeCountLimit) {
            if (isBlowing) {
                tetris.blow(jetStream[jetStreamIndex])
                jetStreamIndex++
                if (jetStreamIndex > jetStream.lastIndex) {
                    jetStreamIndex = 0
                }
            } else {
                if (tetris.fall()) {
                    tetris.nextShape()
                }
            }
            isBlowing = !isBlowing
        }

        val totalHeight = tetris.totalRockHeight()
        return totalHeight
    }

    fun part2(input: List<String>, shapeCountLimit: Long = 1000000000000L): Long {
        val jetStream = input.first().toList()
        var jetStreamIndex = 0
        val tetris = Tetris()
        var isBlowing = true

        val cycleDetection = mutableMapOf<Pair<Int, Int>, Pair<Long,Long>>()
        var doneCycleDetection = false

        while (tetris.shapeCount <= shapeCountLimit) {
            if (isBlowing) {
                tetris.blow(jetStream[jetStreamIndex])
                jetStreamIndex = ++jetStreamIndex % jetStream.lastIndex
            } else {
                if (tetris.fall()) {
                    // each time we blit a new block down to the ground, see if we've repeated this particular gust + shape combo
                    if (!doneCycleDetection  && tetris.shapeCount > 10000L) { // wait until ~10k shapes before looking for a cycle
                        val cacheKey = Pair(jetStreamIndex, tetris.shapeIndex)
                        if (cycleDetection.contains(cacheKey)) {
                            val cacheVal = cycleDetection[cacheKey]!!
                            doneCycleDetection = true
                            // so now, at this point in time, because we've already been here before at this exact shape index and jet index
                            val shapesRemaining = shapeCountLimit - tetris.shapeCount
                            val shapesAddedInCycle = (tetris.shapeCount - cacheVal.first)
                            val heightAddedInCycle = (tetris.totalRockHeight() - cacheVal.second)
                            val cyclesInShapesRemaining = shapesRemaining / shapesAddedInCycle
                            println("Found cycle at ${cacheKey}: ${cacheVal}, which added $shapesAddedInCycle shapes and $heightAddedInCycle height")
                            println("There are $shapesRemaining to process, we can fit $cyclesInShapesRemaining of the cycles into that.")
                            tetris.advanceCycle(shapesAddedInCycle * cyclesInShapesRemaining, heightAddedInCycle * cyclesInShapesRemaining)
                        } else {
                            val cacheVal = Pair(tetris.shapeCount, tetris.totalRockHeight())
                            cycleDetection[cacheKey] = cacheVal
                        }
                    }
                    tetris.nextShape()
                }
            }
            isBlowing = !isBlowing
        }

        val totalHeight = tetris.totalRockHeight()
        return totalHeight
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day17_test")
    check(part1(testInput, 2022L) == 3068L)
    println("Part 1 checked out ok!")
    check(part2(testInput) == 1514285714288L)

    val input = readInput("Day17")
    println(part1(input))
    println(part2(input))
}
