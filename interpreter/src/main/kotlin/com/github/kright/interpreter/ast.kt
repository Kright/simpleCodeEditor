package com.github.kright.interpreter

import com.github.h0tk3y.betterParse.lexer.TokenMatch


class ConcreteSyntaxInfo(val tokenMatch: TokenMatch? = null) {
    // all concrete syntax info are equal, so Id("name", x) == Id("name", y)
    override fun hashCode(): Int = 42
    override fun equals(other: Any?): Boolean = other is ConcreteSyntaxInfo
    override fun toString(): String = tokenMatch.toString()
}


data class Op(val name: String, val info: ConcreteSyntaxInfo = ConcreteSyntaxInfo())


sealed class Expression {
    abstract val info: ConcreteSyntaxInfo
}

data class Id(val name: String, override val info: ConcreteSyntaxInfo = ConcreteSyntaxInfo()) : Expression()
data class BinOp(val left: Expression, val op: Op, val right: Expression) : Expression() {
    override val info: ConcreteSyntaxInfo = op.info
}
data class NSequence(val left: Expression, val right: Expression) : Expression() {
    override val info: ConcreteSyntaxInfo = left.info
}
data class FuncCall(val funcName: Id, val args: List<Expression>) : Expression() {
    override val info: ConcreteSyntaxInfo = funcName.info
}
data class Lambda(val args: List<Id>, val body: Expression) : Expression() {
    override val info: ConcreteSyntaxInfo = args[0].info
}

sealed class NNumber : Expression()
data class NReal(val value: Double, override val info: ConcreteSyntaxInfo = ConcreteSyntaxInfo()) : NNumber()
data class NInt(val value: Long, override val info: ConcreteSyntaxInfo = ConcreteSyntaxInfo()) : NNumber()

sealed class Statement

data class VarDeclaration(val varName: Id, val expression: Expression) : Statement()
data class OutExpr(val expr: Expression) : Statement()
data class PrintString(val string: String) : Statement()

data class Program(val statements: List<Statement>)
