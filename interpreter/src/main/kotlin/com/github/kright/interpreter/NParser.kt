package com.github.kright.interpreter

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser


private typealias Info = ConcreteSyntaxInfo


/**
 * parse string to syntax tree
 * don't check types, names of functions and variables, range of long and double values
 */
class NParser : Grammar<Program>() {
    val varRaw by regexToken("var\\s")
    val outRaw by regexToken("out\\s")
    val printRaw by regexToken("print\\s")
    val assignRaw by literalToken("=")
    val bracketRoundL by literalToken("(")
    val bracketRoundR by literalToken(")")
    val bracketFigL by literalToken("{")
    val bracketFigR by literalToken("}")
    val comma by literalToken(",")
    val arrow by literalToken("->")

    val opAdd by literalToken("+")
    val opSub by literalToken("-")
    val opMul by literalToken("*")
    val opDiv by literalToken("/")
    val opPow by literalToken("^")

    val addSubMaybe: Parser<Op?> by (0..1).times(opAdd or opSub) use { firstOrNull()?.let { Op(it.text, Info(it)) } }

    val whitespace by regexToken("\\s+", ignore = true)

    val idRaw by regexToken("[^\\W\\d]\\w*")
    val id by idRaw use { Id(text, Info(this)) }

    val nRealRaw by regexToken("[\\+\\-]?\\d[\\d_]*\\.[\\d_]*")
    val nReal: Parser<NReal> by (addSubMaybe and nRealRaw).map { (op, real) ->
        val sign = if (op == Op("-")) "-" else ""
        NReal((sign + real.text).filter { it.isDigit() || it == '-' || it == '.' }, Info(real))
    }

    val nIntRaw by regexToken("[\\+\\-]?\\d[\\d_]*")
    val nInt: Parser<NInt> by (addSubMaybe and nIntRaw).map { (op, int) ->
        val sign = if (op == Op("-")) "-" else ""
        NInt((sign + int.text).filter { it.isDigit() || it == '-' }, Info(int))
    }

    val stringRaw by regexToken("\\\"[^\\\"]*\\\"")
    val string: Parser<String> by stringRaw use { text.substring(1, text.length - 1) }

    val number: Parser<NNumber> by (nReal or nInt)

    val lambda: Parser<Lambda> by (oneOrMore(id) and skip(arrow) and parser { expression })
        .map { (ids, body) -> Lambda(ids, body) }

    val expressionInBrackets: Parser<Expression> by
    (skip(bracketRoundL) and parser { expression } and skip(bracketRoundR))

    val sequence: Parser<NSequence> by
    (skip(bracketFigL) and parser { expression } and skip(comma) and parser { expression } and skip(bracketFigR))
        .map { (left, right) -> NSequence(left, right) }

    val funcCall: Parser<FuncCall> by
    (id and skip(bracketRoundL) and separated(lambda or parser { expression }, comma) and skip(bracketRoundR))
        .map { (funcName, args) -> FuncCall(funcName, args.terms) }

    val expressionAtom by (funcCall or id or number or expressionInBrackets or sequence)

    val op by (opAdd or opSub or opMul or opDiv or opPow).map { Op(it.text, Info(it)) }
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

