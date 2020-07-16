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

        fun makeFromLambda(
            lambda: Lambda,
            operators: Map<Op, BinaryOperator>,
            functions: Map<Id, InterpreterFunction>
        ): InterpreterFunction {
            val variables = HashMap<Id, InterpreterValue>()
            val variableTypes = HashMap<Id, TType>()

            val localInterpreter = InterpreterState(
                variables = variables,
                operators = operators,
                functions = functions,
                out = null
            )

            val localAstChecker = AstChecker(
                variables = variableTypes,
                operators = operators,
                functions = functions
            )

            InterpreterState.interpreterCheck(lambda.args.distinct().size == lambda.args.size) {
                "same argument name declared twice in lambda"
            }

            return object : InterpreterFunction {
                override val argsCount: Int
                    get() = lambda.args.size

                override fun invoke(args: List<InterpreterValue>): InterpreterValue {
                    InterpreterState.interpreterCheck(args.size == lambda.args.size) { "wrong arguments count!" }

                    for ((name, value) in lambda.args.zip(args)) {
                        variables[name] = value
                    }

                    return localInterpreter.eval(lambda.body)
                }

                override fun invoke(args: List<TType>): TType {
                    InterpreterState.interpreterCheck(args.size == lambda.args.size) { "wrong arguments count!" }

                    for ((name, type) in lambda.args.zip(args)) {
                        variableTypes[name] = type
                    }

                    return localAstChecker.inferTypes(lambda.body)
                }
            }
        }

        private val mapFunction = object : InterpreterFunction {
            override val argsCount: Int
                get() = 2

            override fun invoke(args: List<InterpreterValue>): InterpreterValue {
                checkArgsCount(args)

                val s = toSequence(args[0])
                val lambda = toLambda(args[1])
                InterpreterState.interpreterCheck(lambda.argsCount == 1) { "lambda should have exactly one argument" }

                val newElems = s.elements.map {
                    lambda.func(listOf(it))
                }

                return VSequence(newElems, newElems.firstOrNull()?.type ?: TNothing)
            }

            override fun invoke(args: List<TType>): TType {
                checkArgsCount(args)

                val s = toSequence(args[0])
                val lambda = toLambda(args[1])
                InterpreterState.interpreterCheck(lambda.lambda.argsCount == 1) { "lambda should have exactly one argument" }

                return TSeq(lambda.lambda.invoke(listOf(s.elements)))
            }
        }

        private val reduceFunction: InterpreterFunction = object : InterpreterFunction {
            override val argsCount: Int
                get() = 3

            override fun invoke(args: List<InterpreterValue>): InterpreterValue {
                InterpreterState.interpreterCheck(args.size == 3) { "function should have exactly 3 arguments" }
                val s = toSequence(args[0])
                val initial = args[1]
                InterpreterState.interpreterCheck(initial !is VLambda) { "second argument should be expression, not lambda" }
                val lambda = toLambda(args[2])
                InterpreterState.interpreterCheck(lambda.argsCount == 2) { "lambda should have exactly two arguments" }

                return s.elements.fold(initial, { acc, v -> lambda.func(listOf(acc, v)) })
            }

            override fun invoke(args: List<TType>): TType {
                InterpreterState.interpreterCheck(args.size == 3) { "function should have exactly 3 arguments" }
                val s = toSequence(args[0])
                val initial = args[1]
                InterpreterState.interpreterCheck(initial !is TLambda) { "second argument should be expression, not lambda" }
                val lambda = toLambda(args[2])

                val result = lambda.lambda(listOf(initial, s.elements))
                InterpreterState.interpreterCheck(result == initial) {
                    "reduction result $result, but expected $initial"
                }
                return result
            }
        }

        private fun <T> InterpreterFunction.checkArgsCount(args: List<T>) =
            InterpreterState.interpreterCheck(args.size == argsCount) {
                "args count should be ${argsCount}, but get ${args.size}"
            }

        private fun toSequence(value: InterpreterValue): VSequence =
            when (value) {
                is VSequence -> value
                else -> throw InterpreterException("expected sequence, get ${value.type}")
            }

        private fun toSequence(type: TType): TSeq =
            when (type) {
                is TSeq -> type
                else -> throw InterpreterException("expected sequence, get $type")
            }

        private fun toLambda(value: InterpreterValue): VLambda =
            when (value) {
                is VLambda -> value
                else -> throw InterpreterException("expected lambda, get ${value.type}")
            }

        private fun toLambda(type: TType): TLambda =
            when (type) {
                is TLambda -> type
                else -> throw InterpreterException("expected lambda, get $type")
            }
    }
}