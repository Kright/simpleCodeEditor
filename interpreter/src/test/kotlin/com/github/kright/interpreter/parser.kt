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

    test("out expression") {
        for (parser in arrayOf(nParser.outExpr, nParser.statement)) {
            parser.apply {
                parseToEnd("out 1") shouldBe OutExpr(NInt(1))
                parseToEnd("out id") shouldBe OutExpr(Id("id"))
                parseToEnd("out -1.3") shouldBe OutExpr(NReal(-1.3))
            }
        }
    }

    test("print string statement") {
        for (parser in arrayOf(nParser.printString, nParser.statement)) {
            parser.parseToEnd("""print "string"""") shouldBe PrintString("string")
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
        for (string in arrayOf("", "a", "string", "12", " ", "12_ w5t\t")) {
            nParser.string.parseToEnd("\"$string\"") shouldBe string
        }
    }

    test("expression in brackets") {
        for (parser in arrayOf(nParser.expressionInBrackets, nParser.expression)) {
            parser.apply {
                parseToEnd("(1)") shouldBe NInt(1)
                parseToEnd("(name)") shouldBe Id("name")
                parseToEnd("(((1.0)))") shouldBe NReal(1.0)
            }
        }
    }

    test("sequence") {
        for (parser in arrayOf(nParser.sequence, nParser.expression)) {
            parser.apply {
                parseToEnd("{1, 2}") shouldBe NSequence(NInt(1), NInt(2))
                parseToEnd("{name, 2.3}") shouldBe NSequence(Id("name"), NReal(2.3))
            }
        }
    }

    test("binary operations") {
        nParser.expression.apply {
            parseToEnd("1 + 2") shouldBe BinOp(NInt(1), Op("+"), NInt(2))
            parseToEnd("1 + -2") shouldBe BinOp(NInt(1), Op("+"), NInt(-2))
            parseToEnd("1 + 2 - 3") shouldBe BinOp(BinOp(NInt(1), Op("+"), NInt(2)), Op("-"), NInt(3))
            parseToEnd("1 + 2 * 3") shouldBe BinOp(NInt(1), Op("+"), BinOp(NInt(2), Op("*"), NInt(3)))
            parseToEnd("1 + 2 * 3 ^ 4") shouldBe BinOp(NInt(1), Op("+"), BinOp(NInt(2), Op("*"), BinOp(NInt(3), Op("^"), NInt(4))))
        }
    }

    test("binary operations no whitespace") {
        nParser.expression.apply {
            parseToEnd("1+2") shouldBe BinOp(NInt(1), Op("+"), NInt(2))
            parseToEnd("1+ -2") shouldBe BinOp(NInt(1), Op("+"), NInt(-2))
            parseToEnd("1+2-3") shouldBe BinOp(BinOp(NInt(1), Op("+"), NInt(2)), Op("-"), NInt(3))
            parseToEnd("1+2*3") shouldBe BinOp(NInt(1), Op("+"), BinOp(NInt(2), Op("*"), NInt(3)))
            parseToEnd("1+2*3^4") shouldBe BinOp(NInt(1), Op("+"), BinOp(NInt(2), Op("*"), BinOp(NInt(3), Op("^"), NInt(4))))
        }
    }

    test("lambda") {
        nParser.lambda.apply {
            parseToEnd("i -> i") shouldBe Lambda(listOf(Id("i")), Id("i"))
            parseToEnd("i -> i + j") shouldBe Lambda(listOf(Id("i")), BinOp(Id("i"), Op("+"), Id("j")))
            parseToEnd("i j -> i + j") shouldBe Lambda(listOf(Id("i"), Id("j")), BinOp(Id("i"), Op("+"), Id("j")))
        }
    }

    test("func call") {
        nParser.funcCall.apply {
            parseToEnd("i(2)") shouldBe FuncCall(Id("i"), listOf(NInt(2)))
            parseToEnd("i ( 2 )") shouldBe FuncCall(Id("i"), listOf(NInt(2)))
            parseToEnd("f(i j -> i + j)") shouldBe FuncCall(Id("f"), listOf(Lambda(listOf(Id("i"), Id("j")), BinOp(Id("i"), Op("+"), Id("j")))))
        }
    }

    test("program") {
        val text = """
            var n = 500
            var sequence = map({0, n}, i -> (-1)^i / (2 * i + 1))
            var pi = 4 * reduce(sequence, 0, x y -> x + y)
            print "pi = "
            out pi
        """.trimIndent()

        val i = Id("i")
        val pi = Id("pi")
        val x = Id("x")
        val y = Id("y")

        val expected = Program(
            listOf(
                VarDeclaration(Id("n"), NInt(500)),
                VarDeclaration(Id("sequence"), FuncCall(Id("map"), listOf(
                    NSequence(NInt(0), Id("n")),
                    Lambda(listOf(i), BinOp(
                        BinOp( NInt(-1), Op("^"), i ),
                        Op("/"),
                        BinOp(BinOp(NInt(2), Op("*"), i), Op("+"), NInt(1))
                ))))),
                VarDeclaration(pi, BinOp(
                    NInt(4),
                    Op("*"),
                    FuncCall(Id("reduce"), listOf(
                        Id("sequence"),
                        NInt(0),
                        Lambda(listOf(x, y), BinOp(x, Op("+"), y))
                    ))
                )),
                PrintString("pi = "),
                OutExpr(pi)
            )
        )

        nParser.parseToEnd(text) shouldBe expected
    }
})