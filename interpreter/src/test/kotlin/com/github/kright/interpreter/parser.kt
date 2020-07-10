package com.github.kright.interpreter

import com.github.h0tk3y.betterParse.parser.Parser
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

    test("id parsing") {
        for (parser in arrayOf(nParser.id, nParser.expression)) {
            for (name in arrayOf("id", "i", "i_2", "i2", "Word", "long_Word_12", "_1")) {
                parser.parseToEnd(name) shouldBe Id(name)
            }
        }
    }

    test("int parsing") {
        for (parser in arrayOf(nParser.nInt, nParser.numberP)) {
            parser.parseToEnd("1") shouldBe NInt(1)
            parser.parseToEnd("125464") shouldBe NInt(125464)
            parser.parseToEnd("+1") shouldBe NInt(1)
            parser.parseToEnd("-2") shouldBe NInt(-2)
            parser.parseToEnd("9_000_000") shouldBe NInt(9000000)
        }
    }

    test("real parsing") {
        for (parser in arrayOf(nParser.nReal, nParser.numberP)) {
            parser.parseToEnd("1.0") shouldBe NReal(1.0)
            parser.parseToEnd("0.1") shouldBe NReal(0.1)
            parser.parseToEnd("0.") shouldBe NReal(0.0)
            parser.parseToEnd("+1.0") shouldBe NReal(1.0)
            parser.parseToEnd("-1.0") shouldBe NReal(-1.0)
            parser.parseToEnd("-0.12") shouldBe NReal(-0.12)
            parser.parseToEnd("-45.12") shouldBe NReal(-45.12)
            parser.parseToEnd("12_34.56_78") shouldBe NReal(1234.5678)
        }
    }
})