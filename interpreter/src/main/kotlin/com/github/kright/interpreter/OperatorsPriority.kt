package com.github.kright.interpreter


/**
 * construct tree of BinOp form lists of terms and separators according to priorities
 * all operators assumed to be left-associative
 * operators at levels[0] have the highest priority
 *
 * for example, for terms [a, b, c, d] and separators [+, *, -] result should be ((a + (b * c)) - d)
 */
class OperatorsPriority(private val levels: List<Set<Op>>) {
    fun usePriorities(terms: List<Expression>, separators: List<Op>): Expression {
        val (resultTerms, resultSeparators) = levels.fold(Pair(terms, separators), this::foldOperators)
        check(resultTerms.size == 1)
        check(resultSeparators.isEmpty())
        return resultTerms[0]
    }

    private fun foldOperators(
        terms: Pair<List<Expression>, List<Op>>,
        opsToFold: Set<Op>
    ): Pair<List<Expression>, List<Op>> {
        val (exprs, ops) = terms
        check(exprs.size == ops.size + 1)
        if (exprs.size == 1) {
            return terms
        }

        val resultExprs = mutableListOf<Expression>(exprs[0])
        val resultOps = mutableListOf<Op>()

        for (i in ops.indices) {
            val expr = exprs[i + 1]
            val op = ops[i]
            if (opsToFold.contains(op)) {
                // change previous expr
                resultExprs[resultExprs.size - 1] = BinOp(resultExprs.last(), op, expr)
            } else {
                resultExprs += expr
                resultOps += op
            }
        }

        return Pair(resultExprs, resultOps)
    }
}
