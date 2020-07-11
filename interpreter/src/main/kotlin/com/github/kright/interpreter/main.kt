package com.github.kright.interpreter

import com.github.h0tk3y.betterParse.grammar.parseToEnd

fun main(args: Array<String>) {
    println("Hello from parser!")
    val p = NParser()

    println(p.parseToEnd("var i = j"))
    println(p.parseToEnd("var i = 2"))
}