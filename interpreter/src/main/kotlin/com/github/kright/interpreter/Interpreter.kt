package com.github.kright.interpreter

import com.github.h0tk3y.betterParse.grammar.parseToEnd

class Interpreter(private val output: Output = Output.default()) {
    private val parser = NParser()

    fun parse(code: String): Program {
        return parser.parseToEnd(code)
    }

    fun run(program: Program) {
        val state = InterpreterState(out = output)
        try {
            for (statement in program.statements) {
                state.run(statement)
            }
        } catch (e: InterpreterException) {
            output.error(e.message ?: "")
            e.printStackTrace()
        }
    }

    fun run(code: String) {
        val program = parse(code)
        run(program)
    }
}