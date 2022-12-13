class Node(var value: Int?, var isList: Boolean = false, val list: MutableList<Node> = mutableListOf()) {
    fun withWrapper(op: ((Node) -> (Int))): Int {
        if (isList) { throw Exception("Can't wrap a list node") }
        isList = true
        list.add(Node(value))
        value = null
        val result = op(this)
        isList = false
        value = list.first().value
        list.clear()
        return result
    }

    override fun toString(): String {
        return if (!isList) {
            value?.toString() ?: ""
        } else {
            "[${list.joinToString(",", transform = Node::toString)}]"
        }
    }
}
fun Regex.splitWithDelimiter(input: CharSequence) = Regex("((?<=%1\$s)|(?=%1\$s))".format(this.pattern)).split(input)

fun main() {
    fun compareTwo(left: Node, right: Node): Int {
        if (!left.isList && !right.isList) {
            return left.value!!.compareTo(right.value!!)
        }

        if (left.isList && right.isList) {
            left.list.indices.forEach { li ->
                if (li > right.list.lastIndex) {
                    return 1
                }

                val result = compareTwo(left.list[li], right.list[li])
                if (result == -1) {
                    return result
                } else if (result == 1) {
                    return result
                }
            }

            return if (left.list.size < right.list.size) {
                -1
            } else {
                0
            }
        }

        return if (!left.isList) {
            left.withWrapper { compareTwo(it, right) }
        } else {
            right.withWrapper { compareTwo(left, it) }
        }
    }

    fun parse(input: String): Node {
        val stack = mutableListOf<Node>()
        var last: Node? = null

        "[\\[\\],]".toRegex().splitWithDelimiter(input).forEach {
            val x = it.toIntOrNull()
            if (it == "[") {
                val node = Node(null, true)
                if (stack.lastOrNull() != null) {
                    stack.last().list.add(node)
                }
                stack.add(node)
            }
            else if (x != null) {
                stack.last().list.add(Node(x))
            }
            else if (it == ",") {
                // noop
            }
            else if (it == "]") {
                last = stack.removeLast()
            }
        }

        return last!!
    }

    fun part1(input: List<String>): Int {
        val nodes = input.windowed(2, 3).flatten().map { parse(it) }

        val indices = mutableListOf<Int>()
        nodes.chunked(2).forEachIndexed { i, pair ->
            if (compareTwo(pair[0], pair[1]) < 1) {
                indices.add(i + 1)
            }
        }

        return indices.sum()
    }

    fun makeDivider(v: Int): Node {
        val outerList = Node(null, true)
        val innerList = Node(null, true)
        val valueNode = Node(v)

        innerList.list.add(valueNode)
        outerList.list.add(innerList)

        return outerList
    }

    fun part2(input: List<String>): Int {
        val dividers = listOf(makeDivider(2), makeDivider(6))
        val nodes = listOf(
            input.windowed(2, 3).flatten().map { parse(it) },
            dividers
        ).flatten().sortedWith { left, right -> compareTwo(left, right) }

        val indices = mutableListOf<Int>()
        nodes.forEachIndexed { i, it ->
            if (dividers.contains(it)) {
                indices.add(i + 1)
            }
        }

        return indices.reduce { acc, i -> acc * i }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day13_test")
    check(part1(testInput) == 13)
    check(part2(testInput) == 140)

    val input = readInput("Day13")
    println(part1(input))
    println(part2(input))
}
