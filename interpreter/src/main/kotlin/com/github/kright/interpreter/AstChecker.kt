package com.github.kright.interpreter

class AstChecker(private val variables: HashMap<Id, TType> = HashMap()) {

    fun check(program: Program) {
        for (st in program.statements) {
            check(st)
        }
    }

    fun copy(): AstChecker = AstChecker(variables.clone() as HashMap<Id, TType>)

    /**
     * if statement invalid, ast checker doesn't change
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

    private fun inferTypes(exp: Expression): TType =
        when (exp) {
            is Id -> variables[exp] ?: throw InterpreterException("use undeclared variable ${exp.name} at ${exp.info}")
            is BinOp -> TNothing // todo
            is NSequence -> TSeq(TInt)
            is FuncCall -> inferTypes(exp)
            is Lambda -> TNothing // todo
            is NReal -> TNothing // todo check conversion
            is NInt -> TNothing // todo check conversion
        }

    private fun inferTypes(funcCall: FuncCall): TType {
        if (funcCall.funcName.name != "reduce" && funcCall.funcName.name != "map") {
            throw InterpreterException("wrong function name at ${funcCall.funcName.info}")
        }
        // todo check args!
        return TNothing
    }
}