package com.github.kright.interpreter

fun main(args: Array<String>) {
    println("args = $args")

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
    Interpreter().run(code)

    Interpreter().run("var seq = {1.0, 2}")

    Interpreter().run("out map(1, 2, 3)")
    Interpreter().run("out {1, 2} + 3")
}