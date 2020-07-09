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
    }
})