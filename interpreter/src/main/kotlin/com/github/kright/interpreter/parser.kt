package com.github.kright.interpreter

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser


class OperatorsPriority(val levels: List<Set<Op>>) {
    fun usePriorities(terms: List<Expression>, separators: List<Op>): Expression {
        val (resultTerms, resultSeparators) = levels.fold(Pair(terms, separators), this::foldOperators)
        check(resultTerms.size == 1)
        check(resultSeparators.isEmpty())
        return resultTerms[0]
    }

    private fun foldOperators(
        terms: Pair<List<Expression>, List<Op>>,
        opsToFold: Set<Op>
    ): Pair<List<Expression>, List<Op>> {
        val (exprs, ops) = terms
        check(exprs.size == ops.size + 1)
        if (exprs.size == 1) {
            return terms
        }

        val resultExprs = mutableListOf<Expression>(exprs[0])
        val resultOps = mutableListOf<Op>()

        for (i in ops.indices) {
            val expr = exprs[i + 1]
            val op = ops[i]
            if (opsToFold.contains(op)) {
                // change previous expr
                resultExprs[resultExprs.size - 1] = BinOp(resultExprs.last(), op, expr)
            } else {
                resultExprs += expr
                resultOps += op
            }
        }

        return Pair(resultExprs, resultOps)
    }
}

class NParser : Grammar<Program>() {
    val varRaw by literalToken("var")
    val outRaw by literalToken("out")
    val printRaw by literalToken("print")
    val assignRaw by literalToken("=")
    val bracketRoundL by literalToken("(")
    val bracketRoundR by literalToken(")")
    val bracketFigL by literalToken("{")
    val bracketFigR by literalToken("}")
    val comma by literalToken(",")

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
    val string: Parser<String> by stringRaw use { text.substring(1, text.length - 1) }

    val number: Parser<NNumber> by (nReal or nInt)

    val expressionInBrackets: Parser<Expression> by
    (skip(bracketRoundL) and parser { expression } and skip(bracketRoundR))

    val sequence: Parser<NSequence> by
    (skip(bracketFigL) and parser { expression } and skip(comma) and parser { expression } and skip(bracketFigR))
        .map { (left, right) -> NSequence(left, right) }

    val expressionAtom by (id or number or expressionInBrackets or sequence)

    val opAdd by literalToken("+")
    val opSub by literalToken("-")
    val opMul by literalToken("*")
    val opDiv by literalToken("/")
    val opPow by literalToken("^")

    val op by (opAdd or opSub or opMul or opDiv or opPow).use { Op(text) }
    val operatorsPriority = OperatorsPriority(
        listOf(
            setOf(Op("^")),
            setOf(Op("*"), Op("/")),
            setOf(Op("+"), Op("-"))
        )
    )

    val expression: Parser<Expression> by separated(expressionAtom, op)
        .use { operatorsPriority.usePriorities(terms, separators) }

    val varDeclaration: Parser<VarDeclaration> by (skip(varRaw) and id and skip(assignRaw) and expression)
        .map { (name, value) -> VarDeclaration(name, value) }

    val outExpr: Parser<OutExpr> by (skip(outRaw) and expression) use { OutExpr(this) }

    val printString: Parser<PrintString> by (skip(printRaw) and string) use { PrintString(this) }

    val statement: Parser<Statement> by (varDeclaration or outExpr or printString)

    override val rootParser: Parser<Program>
        get() = zeroOrMore(statement).map { Program(it) }
}

fun main(args: Array<String>) {
    println("Hello from parser!")
    val p = NParser()

    println(p.parseToEnd("var i = j"))
    println(p.parseToEnd("var i = 2"))
}
