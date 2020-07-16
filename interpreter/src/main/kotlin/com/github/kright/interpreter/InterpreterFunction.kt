package com.github.kright.interpreter

interface InterpreterFunction {
    val argsCount: Int

    operator fun invoke(args: List<InterpreterValue>): InterpreterValue

    operator fun invoke(args: List<TType>): TType

    companion object {
        fun makeDefaultFunctions(): Map<Id, InterpreterFunction> =
            mapOf(
                Id("map") to mapFunction,
                Id("reduce") to reduceFunction
            )

        private val mapFunction = object : InterpreterFunction {
            override val argsCount: Int
                get() = 2

            override fun invoke(args: List<InterpreterValue>): InterpreterValue {
                checkArgsCount(args)

                val s = toSequence(args[0])
                val lambda = toLambda(args[1])
                InterpreterState.interpreterCheck(lambda.argsCount == 1) { "lambda should have exactrly one argument" }

                val newElems = s.elements.map {
                    lambda.func(listOf(it))
                }

                return VSequence(newElems, newElems.firstOrNull()?.type ?: TNothing)
            }

            override fun invoke(args: List<TType>): TType {
                checkArgsCount(args)
                TODO("Not yet implemented")

//                val (seq, lambda) = args
//
//                when (seq) {
//                    is TSeq -> {
//                        when(lambda) {
//                            TLambda ->
//                            else ->
//                        }
//                    }
//                    else -> throw InterpreterException("wrong type of first argument, should be Seq, but get $seq")
//                }
            }
        }

        private val reduceFunction: InterpreterFunction = object : InterpreterFunction{
            override val argsCount: Int
                get() = 3

            override fun invoke(args: List<InterpreterValue>): InterpreterValue {
                InterpreterState.interpreterCheck(args.size == 3) { "function should have exactly 3 arguments" }
                val s = toSequence(args[0])
                val initial = args[1]
                val lambda = toLambda(args[2])
                InterpreterState.interpreterCheck(lambda.argsCount == 2) { "lambda should have exactly two arguments" }

                return s.elements.fold(initial, { acc, v -> lambda.func(listOf(acc, v)) })
            }

            override fun invoke(args: List<TType>): TType {
                TODO("Not yet implemented")
            }

        }

        private fun <T> InterpreterFunction.checkArgsCount(args: List<T>) =
            InterpreterState.interpreterCheck(args.size == argsCount) {
                "args count should be ${argsCount}, but get ${args.size}"
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