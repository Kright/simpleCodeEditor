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

    test("int parsing") {
        nParser.nIntP.parseToEnd("1").shouldBe(NInt(1))
        nParser.nIntP.parseToEnd("125464").shouldBe(NInt(125464))
        nParser.nIntP.parseToEnd("+1").shouldBe(NInt(1))
        nParser.nIntP.parseToEnd("-2").shouldBe(NInt(-2))
        nParser.nIntP.parseToEnd("9_000_000").shouldBe(NInt(9000000))
    }

    test("real parsing") {
        nParser.nRealP.parseToEnd("1.0").shouldBe(NReal(1.0))
        nParser.nRealP.parseToEnd("0.1").shouldBe(NReal(0.1))
        nParser.nRealP.parseToEnd("0.").shouldBe(NReal(0.0))
        nParser.nRealP.parseToEnd("+1.0").shouldBe(NReal(1.0))
        nParser.nRealP.parseToEnd("-1.0").shouldBe(NReal(-1.0))
        nParser.nRealP.parseToEnd("-0.12").shouldBe(NReal(-0.12))
        nParser.nRealP.parseToEnd("-45.12").shouldBe(NReal(-45.12))
        nParser.nRealP.parseToEnd("12_34.56_78").shouldBe(NReal(1234.5678))
    }
})