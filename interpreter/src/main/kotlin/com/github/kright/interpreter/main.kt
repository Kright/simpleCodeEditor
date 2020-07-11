package com.github.kright.interpreter

fun main(args: Array<String>) {
    val interpreter = Interpreter()

    val code = """
        var pi = 3.14
        print "pi = "
        out pi
        var seq = {1, 3}
        out seq
        var piPlus1 = 1 + pi
        out piPlus1
        out map(seq, i -> i*2)
        out reduce({1, 100}, 0, i j -> i + j)
        var seqOfSeq = map({1, 4}, i -> {i * 10, i * 10 + 2})
        out seqOfSeq
        out 2 + 2 * 2
    """.trimIndent()
    interpreter.run(code)

    interpreter.run("var seq = 123.34.5")
    interpreter.run("var seq = {1.0, 2}")
}