class File(
    val path: String,
    val isDirectory: Boolean = false,
    var size: Int = 0,
    var parent: File? = null
) {
    fun addFile(file: File) {
        increaseSize(file)
    }
    fun increaseSize(file: File) {
        size += file.size
        this.parent?.increaseSize(file)
    }
}

fun pathify(it: String): String {
    return it.replace("//", "/")
}

fun main() {
    val root = "/"

    fun parse(input: List<String>): MutableMap<String, File> {
        val tree = mutableMapOf<String, File>()
        tree[root] = File("/", true, 0, null)

        var currentDirectory = root

        fun cd(arg: String) {
            currentDirectory = when(arg) {
                "/" -> root
                ".." -> tree[currentDirectory]!!.parent!!.path
                else -> pathify("$currentDirectory/$arg")
            }

            if (!tree.containsKey(currentDirectory)) {
                throw Exception("No directory $currentDirectory found!")
            }
        }

        val cmdPattern = "\\$ (cd|ls)\\s?(.*)$".toRegex()
        val filePattern = "(dir|\\d+)\\s+(.+?)$".toRegex()

        input.forEach {
            cmdPattern.find(it)?.destructured?.let { (cmd, arg) ->
                if (cmd == "cd") {
                    cd(arg)
                } // ignore "ls" commands... they're assumed
            } ?: run { // assume everything else is the output of "ls" in the current directory
                filePattern.find(it)?.destructured?.let { (a, b) ->
                    if (a == "dir") {
                        val path = pathify("$currentDirectory/$b")
                        tree[currentDirectory]!!.addFile(
                            tree.getOrPut(path) {
                                File(path, true, 0, tree[currentDirectory])
                            }
                        )
                    } else {
                        val path = pathify("$currentDirectory/$b")
                        tree[currentDirectory]!!.addFile(
                            tree.getOrPut(path) {
                                File(path, false, a.toInt(), tree[currentDirectory])
                            }
                        )
                    }
                }
            }
        }

        return tree
    }

    fun part1(input: List<String>): Int {
        val tree = parse(input)
        return tree.filterValues { it.isDirectory && it.size <= 100000 }.values.sumOf { it.size }
    }

    fun part2(input: List<String>): Int {
        val tree = parse(input)
        val unused = 70000000 - tree[root]!!.size
        val toFree = 30000000 - unused
        return tree.filterValues { it.isDirectory && it.size >= toFree }.values.minOf { it.size }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day07_test")
    check(part1(testInput) == 95437)
    check(part2(testInput) == 24933642)

    val input = readInput("Day07")
    println(part1(input))
    println(part2(input))
}
