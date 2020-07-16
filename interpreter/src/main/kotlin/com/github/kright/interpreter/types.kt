package com.github.kright.interpreter

sealed class TType

object TNothing : TType()

object TInt : TType() {
    override fun toString(): String = "Int"
}

object TReal : TType() {
    override fun toString() = "Real"
}

object TLambda : TType() {
    override fun toString() = "Lambda"
}

data class TSeq(val elements: TType): TType() {
    override fun toString(): String = "Seq<${elements}>"
}
