package com.github.kright.interpreter

import kotlin.math.pow

interface BinaryOperator {
    operator fun invoke(first: InterpreterValue, second: InterpreterValue): InterpreterValue

    operator fun invoke(first: TType, second: TType): TType

    companion object {
        fun defaultOperators(): Map<Op, BinaryOperator> = mapOf(
            Op("+") to binOp({ i, j -> i + j }, { i, j -> i + j }),
            Op("-") to binOp({ i, j -> i - j }, { i, j -> i - j }),
            Op("*") to binOp({ i, j -> i * j }, { i, j -> i * j }),
            Op("/") to binOp({ i, j -> i / j }, { i, j -> i / j }),
            Op("^") to powOp
        )

        private fun binOp(fInt: (Long, Long) -> Long, fReal: (Double, Double) -> Double): BinaryOperator {
            return object : BinaryOperator {
                override fun invoke(first: InterpreterValue, second: InterpreterValue): InterpreterValue =
                    when (invoke(first.type, second.type)) {
                        TInt -> VInt(fInt(first.toVInt()!!.value, second.toVInt()!!.value))
                        TReal -> VReal(fReal(toVReal(first)!!.value, toVReal(second)!!.value))
                        else -> wrongTypes(first.type, second.type)
                    }

                override fun invoke(first: TType, second: TType): TType =
                    when (first) {
                        TInt -> when (second) {
                            TInt -> TInt
                            TReal -> TReal
                            else -> wrongTypes(first, second)
                        }
                        TReal -> when (second) {
                            TInt -> TReal
                            TReal -> TReal
                            else -> wrongTypes(first, second)
                        }
                        else -> wrongTypes(first, second)
                    }
            }
        }

        private val powOp: BinaryOperator = object : BinaryOperator {
            override fun invoke(first: InterpreterValue, second: InterpreterValue): InterpreterValue {
                invoke(first.type, second.type)
                return VReal(toVReal(first)!!.value.pow(toVReal(second)!!.value))
            }

            override fun invoke(first: TType, second: TType): TType =
                when (first) {
                    TInt, TReal ->
                        when (second) {
                            TInt, TReal -> TReal
                            else -> wrongTypes(first, second)
                        }
                    else -> wrongTypes(first, second)
                }
        }

        private fun toVReal(v: InterpreterValue): VReal? =
            when (v) {
                is VInt -> VReal(v.value.toDouble())
                is VReal -> v
                else -> null
            }

        private fun wrongTypes(first: TType, second: TType): Nothing =
            throw InterpreterException("wrong types, expected Int or Real, get ($first, $second)")
    }
}
