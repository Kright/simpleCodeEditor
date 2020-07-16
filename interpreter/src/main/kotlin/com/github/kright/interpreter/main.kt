package com.github.kright.interpreter

import java.io.File


fun main(args: Array<String>) {

    if (args.isEmpty()) {
        val output = Output(print = { println(it) }, error = { System.err.println(it) })
        val interpreter = Interpreter(output = output)

        output.print("welcome to REPL mode")
        interpreter.run("out reduce({1,3}, 1.0, i j -> i^j)")
        interpreter.run("out reduce(map({1, 3}, i -> {1, 2}), {1, 3}, i j -> i^j)")
        while (true) {
            readLine()?.also {
                interpreter.run(it)
            }
        }
    } else {
        if (args.size > 1) {
            error("ony one arg allowed: path to file with code")
        }
        val file = File(args[0])
        if (!file.exists()) {
            error("file ${file} doesn't exist, cwd = ${File("").absolutePath}")
        }

        val code = file.readText()
        val interpreter = Interpreter(Output(print = { println(it) }, error = { error(it) }))
        interpreter.run(code)
    }
}