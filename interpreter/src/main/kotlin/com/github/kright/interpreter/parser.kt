package com.github.kright.interpreter

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

class NParser : Grammar<Program>() {
    val nVar by literalToken("var")
    val assign by literalToken("=")
    val ws by regexToken("\\s+", ignore = true)

    val id by regexToken("[^\\W\\d]\\w*")
    val idP by id use { Id(text) }

    val nReal by regexToken("[\\+\\-]?\\d[\\d_]*\\.[\\d_]*")
    val nRealP: Parser<NReal> by nReal use { NReal(text.filter { it.isDigit() || it == '-' || it == '.' }.toDouble()) }

    val nInt by regexToken("[\\+\\-]?\\d[\\d_]*")
    val nIntP: Parser<NInt> by nInt use { NInt(text.filter { it.isDigit() || it == '-' }.toLong()) }

    val expression: Parser<Expression> by (idP or nIntP)

    val statementP by (skip(nVar) and idP and skip(assign) and expression)
        .map { (name, value) -> VarDeclaration(name, value) }

    override val rootParser: Parser<Program>
        get() = zeroOrMore(statementP).map { Program(it) }
}

fun main(args: Array<String>) {
    println("Hello from parser!")
    val p = NParser()

    println(p.parseToEnd("var i = j"))
    println(p.parseToEnd("var i = 2"))
}
