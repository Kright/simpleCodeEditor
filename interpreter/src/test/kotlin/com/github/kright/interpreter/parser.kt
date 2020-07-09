package com.github.kright.interpreter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MyTest : FunSpec({
    test("2 + 2 = 4") {
        add(2, 2) shouldBe 4
    }
})