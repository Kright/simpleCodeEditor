package com.github.kright.interpreter

import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.parser.parse
import com.github.h0tk3y.betterParse.parser.parseToEnd
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ParserTest : FunSpec({
    val nParser = NParser()

    fun <T> Parser<T>.parseToEnd(code: String): T = parseToEnd(nParser.tokenizer.tokenize(code))

    test("var declaration") {
        nParser.parseToEnd("var i = j")
            .shouldBe(Program(listOf(VarDeclaration(Id("i"), Id("j")))))

        nParser.parseToEnd("var abc = 12")
            .shouldBe(Program(listOf(VarDeclaration(Id("abc"), NInt(12)))))
    }

    test("out expression") {
        for(parser in arrayOf(nParser.outExpr, nParser.statement)) {
            parser.apply {
                parseToEnd("out 1") shouldBe OutExpr(NInt(1))
                parseToEnd("out id") shouldBe OutExpr(Id("id"))
                parseToEnd("out -1.3") shouldBe OutExpr(NReal(-1.3))
            }
        }
    }

    test("id parsing") {
        for (parser in arrayOf(nParser.id, nParser.expression)) {
            for (name in arrayOf("id", "i", "i_2", "i2", "Word", "long_Word_12", "_1")) {
                parser.parseToEnd(name) shouldBe Id(name)
            }
        }
    }

    test("int parsing") {
        for (parser in arrayOf(nParser.nInt, nParser.number)) {
            parser.apply {
                parseToEnd("1") shouldBe NInt(1)
                parseToEnd("125464") shouldBe NInt(125464)
                parseToEnd("+1") shouldBe NInt(1)
                parseToEnd("-2") shouldBe NInt(-2)
                parseToEnd("9_000_000") shouldBe NInt(9000000)
            }
        }
    }

    test("real parsing") {
        for (parser in arrayOf(nParser.nReal, nParser.number)) {
            parser.apply {
                parseToEnd("1.0") shouldBe NReal(1.0)
                parseToEnd("0.1") shouldBe NReal(0.1)
                parseToEnd("0.") shouldBe NReal(0.0)
                parseToEnd("+1.0") shouldBe NReal(1.0)
                parseToEnd("-1.0") shouldBe NReal(-1.0)
                parseToEnd("-0.12") shouldBe NReal(-0.12)
                parseToEnd("-45.12") shouldBe NReal(-45.12)
                parseToEnd("12_34.56_78") shouldBe NReal(1234.5678)
            }
        }
    }

    test("string parser") {
        nParser.string.apply {
            parseToEnd("\"\"") shouldBe ""
            parseToEnd("\"string\"") shouldBe "string"
        }
    }
})