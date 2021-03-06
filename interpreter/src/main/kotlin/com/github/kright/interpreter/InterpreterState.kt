package com.github.kright.interpreter


class InterpreterState(
    private val variables: HashMap<Id, InterpreterValue> = HashMap(),
    val operators: Map<Op, BinaryOperator> = BinaryOperator.defaultOperators(),
    val functions: Map<Id, InterpreterFunction> = InterpreterFunction.makeDefaultFunctions(),
    private val out: Output? = Output.default()
) {
    fun run(statement: Statement) {
        when (statement) {
            is PrintString ->
                out?.print?.invoke(statement.string)
            is OutExpr -> {
                val expr = eval(statement.expr)
                out?.print?.invoke(toString(expr))
            }
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

    fun eval(e: Expression): InterpreterValue {
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
            is Lambda -> return VLambda(InterpreterFunction.makeFromLambda(e, operators, functions))
            is NReal -> return toVReal(e)
            is NInt -> return toVInt(e)
        }
    }

    private fun toString(value: InterpreterValue): String =
        when (value) {
            is VInt -> value.value.toString()
            is VReal -> value.value.toString()
            is VSequence -> value.elements.joinToString(", ", "[", "]", transform = { toString(it) })
            is VLambda -> throw InterpreterException("lambda shouldn't be here")
        }

    companion object {
        inline fun interpreterCheck(cond: Boolean, msg: () -> String) {
            if (!cond) {
                throw InterpreterException(msg())
            }
        }
    }
}
