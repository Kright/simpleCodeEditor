package com.github.kright.interpreter

class Output(val print: (String) -> Unit, val error: (String) -> Unit) {
    companion object {
        fun default(): Output = Output({ println(it) }, { println("error: ${it}") })
    }
}
