package com.github.kright.interpreter

import kotlin.math.pow

class Output(val print: (String) -> Unit, val error: (String) -> Unit)

sealed class InterpreterValue

data class VInt(val value: Long) : InterpreterValue()
data class VReal(val value: Double) : InterpreterValue()
data class VSequence(val elements: Sequence<InterpreterValue>) : InterpreterValue()
data class VLambda(val argsCount: Int, val func: (List<InterpreterValue>) -> InterpreterValue) : InterpreterValue()


fun getTypeDescription(v: InterpreterValue): String =
    when (v) {
        is VInt -> "int"
        is VReal -> "real"
        is VSequence -> "sequence"
        is VLambda -> "lambda"
    }


class InterpreterException(reason: String) : RuntimeException(reason)


class InterpreterState(
    val variables: HashMap<Id, InterpreterValue>,
    val operators: Map<Op, (InterpreterValue, InterpreterValue) -> InterpreterValue>,
    val functions: Map<Id, (List<InterpreterValue>) -> InterpreterValue>,
    val out: Output
) {
    fun run(statement: Statement) {
        when (statement) {
            is PrintString ->
                out.print(statement.string)
            is OutExpr ->
                out.print(toString(eval(statement.expr)))
            is VarDeclaration -> {
                if (variables.containsKey(statement.varName)) {
                    throw InterpreterException("variable is already declared: ${statement.varName}")
                }
                variables[statement.varName] = eval(statement.expression)
            }
        }
    }

    private fun eval(e: Expression): InterpreterValue {
        when (e) {
            is Id ->
                return variables[e] ?: throw InterpreterException("variable ${e.name} wasn't declared")
            is BinOp -> {
                val op = operators[e.op] ?: throw InterpreterException("no such operator: ${e.op.name}")
                return op(eval(e.left), eval(e.right))
            }
            is NSequence -> {
                val left = toVInt(eval(e.left))
                val right = toVInt(eval(e.right))
                return VSequence((left.value..right.value).asSequence().map { VInt(it) })
            }
            is FuncCall -> {
                val func = functions[e.funcName] ?: throw InterpreterException("no such function: ${e.funcName.name}")
                return func(e.args.map { eval(it) })
            }
            is Lambda -> return makeVLambda(e)
            is NReal -> return VReal(e.value)
            is NInt -> return VInt(e.value)
        }
    }

    private fun makeVLambda(lambdaExpr: Lambda): VLambda {
        val localInterpreter = InterpreterState(
            variables = HashMap(),
            operators = operators,
            functions = functions,
            out = out
        )

        interpreterCheck(lambdaExpr.args.distinct().size == lambdaExpr.args.size) { "same argument name declared twice in lambda" }

        val func: (List<InterpreterValue>) -> InterpreterValue = { argsList ->
            interpreterCheck(argsList.size == lambdaExpr.args.size) { "wrong arguments count!" }

            for ((name, value) in lambdaExpr.args.zip(argsList)) {
                localInterpreter.variables[name] = value
            }

            localInterpreter.eval(lambdaExpr.body)
        }

        return VLambda(lambdaExpr.args.size, func)
    }

    private fun toString(value: InterpreterValue): String =
        when (value) {
            is VInt -> value.value.toString()
            is VReal -> value.value.toString()
            is VSequence -> value.elements.joinToString(", ", "[", "]", transform = { toString(it) })
            is VLambda -> throw InterpreterException("lambda shouldn't be here")
        }

    companion object {
        fun default(): InterpreterState {
            return InterpreterState(
                variables = HashMap(),
                operators = makeDefaultOperators(),
                functions = makeDefaultFunctions(),
                out = Output({ println(it) }, { println("error: ${it}") })
            )
        }

        private fun binOp(
            f1: (Long, Long) -> Long,
            f2: (Double, Double) -> Double
        ): (InterpreterValue, InterpreterValue) -> InterpreterValue = { a, b ->
            if (a is VInt && b is VInt) {
                VInt(f1(a.value, b.value))
            } else {
                VReal(f2(toVReal(a).value, toVReal(b).value))
            }
        }

        private fun makeDefaultOperators(): Map<Op, (InterpreterValue, InterpreterValue) -> InterpreterValue> =
            mapOf(
                Op("+") to binOp({ i, j -> i + j }, { i, j -> i + j }),
                Op("-") to binOp({ i, j -> i - j }, { i, j -> i - j }),
                Op("*") to binOp({ i, j -> i * j }, { i, j -> i * j }),
                Op("/") to binOp({ i, j -> i / j }, { i, j -> i / j }),
                Op("^") to this::pow
            )

        private fun makeDefaultFunctions(): Map<Id, (List<InterpreterValue>) -> InterpreterValue> =
            mapOf()

        private fun pow(a: InterpreterValue, b: InterpreterValue) = VReal(toVReal(a).value.pow(toVReal(b).value))

        private fun map(args: List<InterpreterValue>): InterpreterValue {
            interpreterCheck(args.size == 2) { "function should have exactly 2 arguments" }
            val s = toSequence(args[0])
            val lambda = toLambda(args[1])
            interpreterCheck(lambda.argsCount == 1) { "lambda should have exactrly one argument" }

            return VSequence(s.elements.map {
                lambda.func(listOf(it))
            })
        }

        private fun reduce(args: List<InterpreterValue>): InterpreterValue {
            interpreterCheck(args.size == 3) { "function should have exactly 3 arguments" }
            val s = toSequence(args[0])
            val initial = args[1]
            val lambda = toLambda(args[2])
            interpreterCheck(lambda.argsCount == 2) { "lambda should have exactly one argument" }

            return s.elements.fold(initial, { acc, v -> lambda.func(listOf(acc, v)) })
        }

        private inline fun interpreterCheck(cond: Boolean, msg: () -> String) {
            if (!cond) {
                throw InterpreterException(msg())
            }
        }

        private fun toVInt(v: InterpreterValue): VInt =
            when (v) {
                is VInt -> v
                else -> throw InterpreterException("expected int, get ${getTypeDescription(v)}")
            }

        private fun toVReal(v: InterpreterValue): VReal =
            when (v) {
                is VInt -> VReal(v.value.toDouble())
                is VReal -> v
                else -> throw InterpreterException("expected real, get ${getTypeDescription(v)}")
            }

        private fun toSequence(v: InterpreterValue): VSequence =
            when (v) {
                is VSequence -> v
                else -> throw InterpreterException("expected sequence, get ${getTypeDescription(v)}")
            }

        private fun toLambda(v: InterpreterValue): VLambda =
            when (v) {
                is VLambda -> v
                else -> throw InterpreterException("expected lambda, get ${getTypeDescription(v)}")
            }
    }
}
