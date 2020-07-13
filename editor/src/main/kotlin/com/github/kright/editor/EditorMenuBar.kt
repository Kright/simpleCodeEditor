package com.github.kright.editor

import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuBar

class EditorMenuBar(frame: JFrame) : JMenuBar() {
    init {
        // http://java-online.ru/swing-menu.xhtml

        add(JMenu("File").apply {
            add("open")
            add("save")
            add("save as")
            addSeparator()
            add(
                makeAction("exit") {
                    frame.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING))
                }
            )
        })

        add(JButton("run"))
    }
}