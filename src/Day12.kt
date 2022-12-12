class Grid(val width: Int, val src: Int, val dst: Int, val nodes: List<Int>) {
    fun neighbors(at: Int): List<Int> {
        val list = mutableListOf<Int>()

        if ((at % width) > 0) {
            list.add(at - 1)
        }

        if ((at % width) < (width - 1)) {
            list.add(at + 1)
        }

        if (at >= width) {
            list.add(at - width)
        }

        if (at < (nodes.size - width)) {
            list.add(at + width)
        }

        return list.filter { nodes[it] <= nodes[at] + 1 }
    }

    fun print(path: List<Int>? = null) {
        println("Grid: $width x ${nodes.size / width}, src: $src, dst: $dst")
        nodes.chunked(width).forEachIndexed { y, row ->
            row.forEachIndexed { x, c ->
                val idx = x + y * width
                if (idx == (path?.first() ?: src)) {
                    print("S")
                } else if (idx == (path?.last() ?: dst)) {
                    print("E")
                } else if (path?.contains(idx) == true) {
                    print("#")
                } else {
                    print(".")
                }
            }
            print("\n")
        }
    }

    fun shortestPathFrom(start: Int = src): List<Int> {
        val visited = mutableSetOf<Int>()
        val distances = nodes.indices.associateWith { Int.MAX_VALUE }.toMutableMap()
        val previous: MutableMap<Int, Int?> = nodes.indices.associateWith { null }.toMutableMap()

        val queue = mutableSetOf(start)
        visited.add(start)
        distances[start] = 0

        while (queue.isNotEmpty()) {
            val at = queue.minBy { distances[it]!! }
            queue.remove(at)

            if (at == dst) {
                break
            }

            neighbors(at).filter { !visited.contains(it) }.forEach {
                val d = distances[at]!! + 1
                if (d < distances[it]!!) {
                    distances[it] = d
                    previous[it] = at
                }
                queue.add(it)
            }

            visited.add(at)
        }

        val path = mutableListOf<Int>()
        var at = dst as Int?

        while (at != null && at != start && previous[at] != null) {
            path.add(at)
            at = previous[at]
        }

        return path.reversed()
    }
}
fun main() {
    fun parseGrid(input: List<String>): Grid {
        var src: Int = -1
        var dst: Int = -1

        val nodes = input.flatMapIndexed { y, line ->
            line.toCharArray().mapIndexed { x, c ->
                val code = when (c) {
                    'S' -> 'a'.code
                    'E' -> 'z'.code
                    else -> c.code
                }
                val idx = x + y * line.length
                if (c == 'S') src = idx else if (c == 'E') dst = idx
                code
            }
        }

        return Grid(input.first().length, src, dst, nodes)
    }

    fun part1(input: List<String>): Int {
        val grid = parseGrid(input)
        val path = grid.shortestPathFrom()
        grid.print(path)

        return path.size
    }

    fun part2(input: List<String>): Int {
        val grid = parseGrid(input)

        val starts = grid.nodes.indices.filter { grid.nodes[it] == 'a'.code }
        var path: List<Int>? = null

        starts.forEach {
            val candidate = grid.shortestPathFrom(it)

            if (path == null || candidate.size in 1 until path!!.size) {
                path = candidate
            }
        }

        grid.print(path)
        return path!!.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day12_test")
    check(part1(testInput) == 31)
    check(part2(testInput) == 29)

    val input = readInput("Day12")
    println(part1(input))
    println(part2(input))
}
