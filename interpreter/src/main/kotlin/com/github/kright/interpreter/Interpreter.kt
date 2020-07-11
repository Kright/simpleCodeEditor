package com.github.kright.interpreter

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException


class Interpreter(private val output: Output = Output.default()) {
    private val parser = NParser()

    fun parse(code: String): Program? {
        return try {
            parser.parseToEnd(code)
        } catch (e: ParseException) {
            output.error(e.errorResult.toString())
            null
        }
    }

    fun run(program: Program) {
        val state = InterpreterState(out = output)
        try {
            for (statement in program.statements) {
                state.run(statement)
            }
        } catch (e: InterpreterException) {
            output.error(e.message ?: "")
//            e.printStackTrace()
        }
    }

    fun run(code: String) {
        val program = parse(code)
        program?.also { run(it) }
    }
}