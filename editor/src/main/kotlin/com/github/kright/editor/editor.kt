package com.github.kright.editor

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.WindowEvent
import javax.swing.*


fun createAndShowGUI() {
    val frame = JFrame("code editor").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }

    val menuBar = JMenuBar().apply {
        preferredSize = Dimension(200, 20)

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

    val codeEditor = CodeEditor()

    val programOutput = ProgramOutput()
    programOutput.println("program output")
    programOutput.error("error text")

    frame.jMenuBar = menuBar
    frame.contentPane.add(JScrollPane(codeEditor), BorderLayout.CENTER)
    frame.contentPane.add(JScrollPane(programOutput), BorderLayout.PAGE_END)

    frame.pack()
    frame.isVisible = true
}

fun main(args: Array<String>) {
    // reference: https://docs.oracle.com/javase/tutorial/uiswing/components/index.html
    SwingUtilities.invokeLater {
        createAndShowGUI()
    }
}


fun makeAction(name: String, action: (ActionEvent?) -> Unit): Action =
    object : AbstractAction(name) {
        override fun actionPerformed(e: ActionEvent?) = action(e)
    }
