class Droplet(val points: Set<Triple<Int,Int,Int>>) {
    val minZ = points.minOf { it.third }
    val maxZ = points.maxOf { it.third }
    val minY = points.minOf { it.second }
    val maxY = points.maxOf { it.second }
    val minX = points.minOf { it.first }
    val maxX = points.maxOf { it.first }

    val wormholes = mutableSetOf<Triple<Int,Int,Int>>()
    val holes = mutableSetOf<Triple<Int,Int,Int>>()

    init {
        for (z in minZ..maxZ) {
            for (y in minY..maxY) {
                for (x in minX..maxX) {
                    val point = Triple(x, y, z)
                    if (!points.contains(point)) {
                        if (isEdge(point)) {
                            wormholes.add(point)
                        } else {
                            holes.add(point)
                        }
                    }
                }
            }
        }
    }

    fun scanWormholes() {
        for (wormhole in wormholes.toList()) {
            explore(wormhole)
        }
    }

    fun explore(point: Triple<Int, Int, Int>) {
        val neighbors = neighbors(point)
        val holesConnected = neighbors.filter {
            !points.contains(it) && !wormholes.contains(it)
        }.toSet()
        wormholes.addAll(holesConnected)
        holes.removeAll(holesConnected)

        for (neighbor in holesConnected) {
            explore(neighbor)
        }
    }

    fun neighbors(point: Triple<Int, Int, Int>, filterEnclosed: Boolean = true): Set<Triple<Int,Int,Int>> {
        val (x,y,z) = point
        val neighbors = setOf(
            Triple(x - 1, y, z),
            Triple(x + 1, y, z),
            Triple(x, y - 1, z),
            Triple(x, y + 1, z),
            Triple(x, y, z - 1),
            Triple(x, y, z + 1),
        )

        if (filterEnclosed) {
            return neighbors.filter { encloses(it) }.toSet()
        }

        return neighbors
    }

    fun encloses(point: Triple<Int,Int,Int>): Boolean {
        return point.first in minX..maxX &&
                point.second in minY..maxY &&
                point.third in minZ..maxZ
    }

    fun isEdge(point: Triple<Int,Int,Int>): Boolean {
        return point.first == minX || point.first == maxX ||
                point.second == minY || point.second == maxY ||
                point.third == minZ || point.third == maxZ
    }

    companion object {
        fun from(input: List<String>): Droplet {
            val points = mutableSetOf<Triple<Int, Int, Int>>()
            input.forEach { line ->
                val (x, y, z) = line.split(",").map(String::toInt)
                val point = Triple(x, y, z)
                points.add(point)
            }
            return Droplet(points)
        }
    }
}
fun main() {
    fun part1(input: List<String>): Int {
        val droplet = Droplet.from(input)
        val surface = droplet.points.sumOf {
            droplet.neighbors(it, false).filter { n ->
                !droplet.points.contains(n)
            }.size
        }
        return surface
    }

    fun part2(input: List<String>): Int {
        val droplet = Droplet.from(input)
        droplet.scanWormholes()
        val surfaceArea = droplet.points.sumOf { droplet.neighbors(it, false).filter { n -> !droplet.points.contains(n) }.size }
        val interiors = droplet.holes.sumOf { droplet.neighbors(it).filter { n -> droplet.points.contains(n) }.size }

        return surfaceArea - interiors
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day18_test")
    check(part1(testInput) == 64)
    println("Part 1 checked out ok!")
    check(part2(testInput) == 58)
    println("Part 2 checked out ok!")


    val input = readInput("Day18")
    println(part1(input))
    println(part2(input))
}
