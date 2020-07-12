package com.github.kright.interpreter

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException


class Interpreter(private val output: Output = Output.default()) {
    private val parser = NParser()
    private val state = InterpreterState(out = output)

    fun parse(code: String): Program? {
        return try {
            parser.parseToEnd(code)
        } catch (e: ParseException) {
            output.error(e.errorResult.toString())
            null
        }
    }

    fun run(program: Program) {
        try {
            for (statement in program.statements) {
                state.run(statement)
            }
        } catch (e: InterpreterException) {
            output.error(e.message ?: "")
        }
    }

    fun run(code: String) {
        val program = parse(code)
        program?.also { run(it) }
    }
}