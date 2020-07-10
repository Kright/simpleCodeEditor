package com.github.kright.interpreter

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser

class NParser : Grammar<Program>() {
    val varRaw by literalToken("var")
    val outRaw by literalToken("out")
    val printRaw by literalToken("print")
    val assignRaw by literalToken("=")
    val bracketRoundL by literalToken("(")
    val bracketRoundR by literalToken(")")
    val bracketFigL by literalToken("{")
    val bracketFigR by literalToken("}")

    val ws by regexToken("\\s+", ignore = true)

    val idRaw by regexToken("[^\\W\\d]\\w*")
    val id by idRaw use { Id(text) }

    val nRealRaw by regexToken("[\\+\\-]?\\d[\\d_]*\\.[\\d_]*")
    val nReal: Parser<NReal> by nRealRaw use {
        NReal(text.filter { it.isDigit() || it == '-' || it == '.' }.toDouble())
    }

    val nIntRaw by regexToken("[\\+\\-]?\\d[\\d_]*")
    val nInt: Parser<NInt> by nIntRaw use { NInt(text.filter { it.isDigit() || it == '-' }.toLong()) }

    val stringRaw by regexToken("\\\"[^\\\"]*\\\"")
    val string: Parser<String> by stringRaw use { text.substring(1, text.length - 1).toString() }

    val number: Parser<NNumber> by (nReal or nInt)

    val expression: Parser<Expression> by (id or number)

    val varDeclaration: Parser<VarDeclaration> by (skip(varRaw) and id and skip(assignRaw) and expression)
        .map { (name, value) -> VarDeclaration(name, value) }

    val outExpr: Parser<OutExpr> by (skip(outRaw) and expression) use { OutExpr(this) }

    val statement: Parser<Statement> by (varDeclaration or outExpr)

    override val rootParser: Parser<Program>
        get() = zeroOrMore(statement).map { Program(it) }
}

fun main(args: Array<String>) {
    println("Hello from parser!")
    val p = NParser()

    println(p.parseToEnd("var i = j"))
    println(p.parseToEnd("var i = 2"))
}
