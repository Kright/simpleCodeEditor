package com.github.kright.interpreter

data class Op(val name: String)

sealed class Expression
data class Id(val name: String) : Expression()
data class BinOp(val left: Expression, val op: Op, val right: Expression) : Expression()
data class NSequence(val left: Expression, val right: Expression) : Expression()
data class FuncCall(val funcName: Id, val args: List<Expression>) : Expression()
data class Lambda(val args: List<Id>, val body: Expression) : Expression()

sealed class NNumber : Expression()
data class NReal(val value: Double) : NNumber()
data class NInt(val value: Long) : NNumber()

sealed class Statement

data class VarDeclaration(val varName: Id, val expression: Expression) : Statement()
data class OutExpr(val expr: Expression) : Statement()
data class PrintString(val string: String) : Statement()

data class Program(val statements: List<Statement>)
