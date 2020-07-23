package com.github.kright.interpreter

import java.io.File


fun main(args: Array<String>) {
    val output = Output(print = { println(it) }, error = { System.err.println(it) })
    val interpreter = Interpreter(output = output)

    when (args.size) {
        0 -> runREPL(output, interpreter)
        1 -> interpreter.run(readCodeFromFile(args[0]))
        2 -> {
            if (args[0] == "-c") {
                interpreter.run(args[1])
            } else {
                error("unknown arg: ${args[0]}")
            }
        }
        else -> error("unknown args: ${args}")
    }
}

private fun runREPL(output: Output, interpreter: Interpreter) {
    output.print("welcome to REPL mode")
    while (true) {
        readLine()?.also {
            interpreter.run(it)
        }
    }
}

private fun readCodeFromFile(fileName: String): String {
    val file = File(fileName)
    if (!file.exists()) {
        error("file $file doesn't exist, cwd = ${File("").absolutePath}")
    }

    return file.readText()
}
