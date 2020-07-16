package com.github.kright.interpreter

/**
 * data classes for internal data representation in interpreter
 */
sealed class InterpreterValue {
    abstract val type: TType

    fun toVInt(): VInt? =
        when (this) {
            is VInt -> this
            else -> null
        }
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

data class VLambda(val interpreterFunction: InterpreterFunction) : InterpreterValue() {
    val argsCount: Int
        get() = interpreterFunction.argsCount

    val func: (List<InterpreterValue>) -> InterpreterValue = interpreterFunction::invoke

    override val type: TType
        get() = TLambda(interpreterFunction)
}


internal fun toVReal(e: NReal): VReal = try {
    VReal(e.value.toDouble())
} catch (ex: NumberFormatException) {
    throw InterpreterException("\"${e.value}\" at ${e.info} isn't a valid Real value").apply {
        addSuppressed(ex)
    }
}

internal fun toVInt(e: NInt): VInt = try {
    VInt(e.value.toLong())
} catch (ex: NumberFormatException) {
    throw InterpreterException("\"${e.value}\" at ${e.info} isn't a valid Int value").apply {
        addSuppressed(ex)
    }
}
