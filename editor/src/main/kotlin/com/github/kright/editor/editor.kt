package com.github.kright.editor

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext


fun makeAction(name: String, action: (ActionEvent?) -> Unit): Action =
    object : AbstractAction(name) {
        override fun actionPerformed(e: ActionEvent?) = action(e)
    }


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

    val codeEditor = JCodeEditor()

    val programOutput = JProgramOutput()
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

class JProgramOutput : JTextPane() {
    init {
        margin = Insets(5, 5, 5, 5)
        isEditable = false
    }

    fun appendText(text: String, color: Color) {
        val sc = StyleContext.getDefaultStyleContext()
        var aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color)
        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        isEditable = true
        caretPosition = document.length
        setCharacterAttributes(aset, false)
        replaceSelection(text)
        isEditable = false
    }

    fun println(text: String) = appendText(text + "\n", Color.black)
    fun error(text: String) = appendText(text + "\n", Color.red)
    fun clear() {
        isEditable = true
        text = ""
        isEditable = false
    }
}

class JCodeEditor : JTextPane() {
}
