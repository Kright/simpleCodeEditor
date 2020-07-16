package com.github.kright.interpreter

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException


class Interpreter(private val output: Output = Output.default()) {
    private val parser = NParser()
    private val astChecker = AstChecker()
    private val state = InterpreterState(out = output)

    fun parse(code: String): Program? {
        return try {
            val program = parser.parseToEnd(code)
            // code parsing doesn't change interpreter state.
            // I copy astChecker because it saves types of all declared variables and them should be consistent with
            // interpreter state
            astChecker.copy().check(program)
            program
        } catch (ex: ParseException) {
            output.error(ex.errorResult.toString())
            null
        } catch (ex: InterpreterException) {
            println("wtf?")
            output.error(ex.message ?: "")
            println("wtf2!")
            null
        }
    }

    fun run(program: Program): Boolean {
        return try {
            for (statement in program.statements) {
                // list of declared variables in astChecker and state will be consistent
                // program was already checked while parsing, so it should be valid
                astChecker.check(statement)
                state.run(statement)
            }
            true
        } catch (e: InterpreterException) {
            output.error(e.message ?: "")
            false
        }
    }

    fun run(code: String): Boolean {
        val program = parse(code) ?: return false
        return run(program)
    }
}