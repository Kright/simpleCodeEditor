package com.github.kright.editor

import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Action

fun makeAction(name: String, action: (ActionEvent?) -> Unit): Action =
    object : AbstractAction(name) {
        override fun actionPerformed(e: ActionEvent?) = action(e)
    }