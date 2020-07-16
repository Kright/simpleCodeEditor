package com.github.kright.interpreter


class InterpreterException(reason: String) : RuntimeException(reason)


class InterpreterState(
    private val variables: HashMap<Id, InterpreterValue> = HashMap(),
    private val operators: Map<Op, BinaryOperator> = BinaryOperator.defaultOperators(),
    private val functions: Map<Id, (List<InterpreterValue>) -> InterpreterValue> = makeDefaultFunctions(),
    private val out: Output = Output.default()
) {
    fun run(statement: Statement) {
        when (statement) {
            is PrintString ->
                out.print(statement.string)
            is OutExpr ->
                out.print(toString(eval(statement.expr)))
            is VarDeclaration -> {
                if (variables.containsKey(statement.varName)) {
                    throw InterpreterException(
                        "variable is already declared: ${statement.varName} at ${statement.varName.info}"
                    )
                }
                variables[statement.varName] = eval(statement.expression)
            }
        }
    }

    private fun eval(e: Expression): InterpreterValue {
        when (e) {
            is Id ->
                return variables[e] ?: throw InterpreterException("variable ${e.name} wasn't declared at ${e.info}")
            is BinOp -> {
                val op = operators[e.op] ?: throw InterpreterException("no such operator: ${e.op.name} at ${e.op.info}")
                return try {
                    op(eval(e.left), eval(e.right))
                } catch (ex: InterpreterException) {
                    throw InterpreterException(ex.message + "\nfor ${e.op.name} at ${e.op.info}")
                }
            }
            is NSequence -> {
                val left =
                    eval(e.left).toVInt() ?: throw InterpreterException("expected int in sequence at ${e.left.info}")
                val right =
                    eval(e.right).toVInt() ?: throw InterpreterException("expected int in sequence at ${e.right.info}")
                return VSequence((left.value..right.value).asSequence().map { VInt(it) }.toList(), TInt)
            }
            is FuncCall -> {
                val func = functions[e.funcName]
                    ?: throw InterpreterException("no such function: ${e.funcName.name} at ${e.funcName.info}")
                try {
                    return func(e.args.map { eval(it) })
                } catch (ex: InterpreterException) {
                    throw InterpreterException(ex.message + "\nfor function ${e.funcName.name} at ${e.info}")
                }
            }
            is Lambda -> return makeVLambda(e)
            is NReal -> return try {
                VReal(e.value.toDouble())
            } catch (ex: NumberFormatException) {
                throw InterpreterException("\"${e.value}\" at ${e.info} isn't a valid Real value").apply {
                    addSuppressed(ex)
                }
            }
            is NInt -> return try {
                VInt(e.value.toLong())
            } catch (ex: NumberFormatException) {
                throw InterpreterException("\"${e.value}\" at ${e.info} isn't a valid Int value").apply {
                    addSuppressed(ex)
                }
            }
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
        private fun makeDefaultFunctions(): Map<Id, (List<InterpreterValue>) -> InterpreterValue> =
            mapOf(
                Id("map") to this::map,
                Id("reduce") to this::reduce
            )

        private fun map(args: List<InterpreterValue>): InterpreterValue {
            interpreterCheck(args.size == 2) { "function should have exactly 2 arguments" }
            val s = toSequence(args[0])
            val lambda = toLambda(args[1])
            interpreterCheck(lambda.argsCount == 1) { "lambda should have exactrly one argument" }

            val newElems = s.elements.map {
                lambda.func(listOf(it))
            }

            return VSequence(newElems, newElems.firstOrNull()?.type ?: TNothing)
        }

        private fun reduce(args: List<InterpreterValue>): InterpreterValue {
            interpreterCheck(args.size == 3) { "function should have exactly 3 arguments" }
            val s = toSequence(args[0])
            val initial = args[1]
            val lambda = toLambda(args[2])
            interpreterCheck(lambda.argsCount == 2) { "lambda should have exactly two arguments" }

            return s.elements.fold(initial, { acc, v -> lambda.func(listOf(acc, v)) })
        }

        private inline fun interpreterCheck(cond: Boolean, msg: () -> String) {
            if (!cond) {
                throw InterpreterException(msg())
            }
        }

        private fun toSequence(v: InterpreterValue): VSequence =
            when (v) {
                is VSequence -> v
                else -> throw InterpreterException("expected sequence, get ${v.type}")
            }

        private fun toLambda(v: InterpreterValue): VLambda =
            when (v) {
                is VLambda -> v
                else -> throw InterpreterException("expected lambda, get ${v.type}")
            }
    }
}
