package com.github.kright.editor

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Insets
import javax.swing.*
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext


fun createAndShowGUI() {
    val frame = JFrame("code editor").apply {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }

    val menuBar = JMenuBar().apply {
        preferredSize = Dimension(200, 20)
    }

    val programOutput = JProgramOutput()
    programOutput.println("program output")
    programOutput.error("error text")

    val programCode = JTextPane()

    frame.jMenuBar = menuBar
    frame.contentPane.add(JScrollPane(programCode), BorderLayout.CENTER)
    frame.contentPane.add(JScrollPane(programOutput), BorderLayout.PAGE_END)

    frame.pack()
    frame.isVisible = true
}

fun main(args: Array<String>) {
    // reference: https://docs.oracle.com/javase/tutorial/uiswing/components/index.html
    println("Hello world!")

    SwingUtilities.invokeLater {
        createAndShowGUI()
    }
}

class JProgramOutput: JTextPane() {
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
}
