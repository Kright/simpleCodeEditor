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
    val nInt by regexToken("[\\+\\-]?\\d[\\d_]*")
    val nIntP: Parser<NInt> by (nInt.map { NInt(it.text.toLong()) })
    val idP by id.map { Id(it.text) }

    val expression: Parser<Expression> by (idP or nIntP)

    val statement by (skip(nVar) and idP and skip(assign) and expression)
        .map { (name, value) -> VarDeclaration(name, value) }

    override val rootParser: Parser<Program>
        get() = zeroOrMore(statement).map { Program(it) }
}

fun main(args: Array<String>) {
    println("Hello from parser!")
    val p = NParser()

    println(p.parseToEnd("var i = j"))
    println(p.parseToEnd("var i = 2"))
}
