package com.github.kright.interpreter

class AstChecker(
    private val variables: HashMap<Id, TType> = HashMap(),
    private val operators: Map<Op, BinaryOperator>,
    private val functions: Map<Id, InterpreterFunction>
) {
    fun check(program: Program) {
        for (st in program.statements) {
            check(st)
        }
    }

    fun copy(): AstChecker = AstChecker(
        variables = variables.clone() as HashMap<Id, TType>,
        operators = operators,
        functions = functions
    )

    /**
     * if statement invalid, ast checker remains constant
     */
    fun check(statement: Statement) {
        when (statement) {
            is PrintString -> Unit
            is OutExpr -> inferTypes(statement.expr)
            is VarDeclaration -> {
                if (variables.containsKey(statement.varName)) {
                    throw InterpreterException("variable ${statement.varName.name} is already declared: at ${statement.varName.info}")
                }
                variables[statement.varName] = inferTypes(statement.expression)
            }
        }
    }

    internal fun inferTypes(exp: Expression): TType =
        when (exp) {
            is Id -> variables[exp] ?: throw InterpreterException("use undeclared variable ${exp.name} at ${exp.info}")
            is BinOp -> inferTypes(exp)
            is NSequence -> TSeq(TInt)
            is FuncCall -> inferTypes(exp)
            is Lambda -> TLambda(InterpreterFunction.makeFromLambda(exp, operators, functions))
            is NReal -> toVReal(exp).type
            is NInt -> toVInt(exp).type
        }

    private fun inferTypes(funcCall: FuncCall): TType =
        functions[funcCall.funcName]?.let { func ->
            val argsTypes = funcCall.args.map { inferTypes(it) }
            func(argsTypes)
        } ?: throw InterpreterException("wrong function name at ${funcCall.funcName.info}")

    private fun inferTypes(binOp: BinOp): TType =
        operators[binOp.op]?.let { opFunc ->
            try {
                opFunc(inferTypes(binOp.left), inferTypes(binOp.right))
            } catch (ex: InterpreterException) {
                throw InterpreterException(ex.message + " at ${binOp.op.info}")
            }
        } ?: throw InterpreterException("invalid operator ${binOp.op.name} at ${binOp.op.info}")
}
