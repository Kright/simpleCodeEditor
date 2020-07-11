package com.github.kright.interpreter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

private class AllOutput {
    val cout = ArrayList<String>()
    val cerr = ArrayList<String>()
    val output = Output({ cout += it }, { cerr += it })
}

class InterpreterTest : FunSpec({
    fun run(code: String): List<String> {
        val allOutput = AllOutput()
        val interpreter = Interpreter(allOutput.output)
        interpreter.run(code)
        allOutput.cerr.shouldBeEmpty()
        return allOutput.cout.toList()
    }

    test("print msg") {
        run("print \"hi\"") shouldBe listOf("hi")
    }

    test("out expr") {
        run("out 1") shouldBe listOf("1")
        run("out 1 + 2") shouldBe listOf("3")
        run("out 1 + 2 * 3 + 6") shouldBe listOf("13")
    }

    test("var") {
        run("var pi = 3.14") shouldBe listOf()
        run("var one = 1 out 1") shouldBe listOf("1")
    }

    test("map") {
        run("out map({1, 3}, i -> i * 2)") shouldBe listOf("[2, 4, 6]")
        run("out map(map({1, 3}, i -> i * 2), i -> i / 2)") shouldBe listOf("[1, 2, 3]")
    }

    test("reduce") {
        run("out reduce({1, 4}, 0, i j -> i + j)") shouldBe listOf("10")
    }
})