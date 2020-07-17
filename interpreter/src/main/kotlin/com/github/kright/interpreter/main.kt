package com.github.kright.interpreter

import java.io.File


fun main(args: Array<String>) {
    val output = Output(print = { println(it) }, error = { System.err.println(it) })
    val interpreter = Interpreter(output = output)

    if (args.isEmpty()) {
        output.print("welcome to REPL mode")
        interpreter.run(File("/home/lgor/Desktop/prog2.py").readText())
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
        interpreter.run(code)
    }
}