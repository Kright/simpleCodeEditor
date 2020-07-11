package com.github.kright.interpreter

sealed class TType
object TAny: TType()
object TNothing: TType()
object TInt : TType()
object TReal: TType()
data class TSequence(val type: TType)