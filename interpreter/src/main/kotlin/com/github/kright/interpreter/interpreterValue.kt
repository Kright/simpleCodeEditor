package com.github.kright.interpreter

/**
 * data classes for internal data representation in interpreter
 */
sealed class InterpreterValue {
    abstract val type: TType
}

data class VInt(val value: Long) : InterpreterValue() {
    override val type: TType
        get() = TInt
}

data class VReal(val value: Double) : InterpreterValue() {
    override val type: TType
        get() = TReal
}

data class VSequence(val elements: List<InterpreterValue>, override val type: TType) : InterpreterValue()

data class VLambda(val argsCount: Int, val func: (List<InterpreterValue>) -> InterpreterValue) : InterpreterValue() {
    override val type: TType
        get() = TLambda
}
