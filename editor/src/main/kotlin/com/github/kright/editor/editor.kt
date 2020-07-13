package com.github.kright.editor

import java.awt.BorderLayout
import java.awt.event.WindowEvent
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.SwingUtilities


class CodeEditorJFrame() : JFrame("code editor") {

    val menuBar: EditorMenuBar
    val codeEditor: CodeEditor
    val programOutput: ProgramOutput

    init {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        menuBar = EditorMenuBar(this)
        codeEditor = CodeEditor(this)

        programOutput = ProgramOutput().apply {
            println("program output")
            error("error text")
        }

        jMenuBar = menuBar
        contentPane.add(JScrollPane(codeEditor), BorderLayout.CENTER)
        contentPane.add(JScrollPane(programOutput), BorderLayout.PAGE_END)


        pack()
        isVisible = true
    }

    fun menuOpenFile() {
        JFileChooser(codeEditor.currentFile?.parentFile).apply {
            dialogTitle = "select file to open"
            when (showOpenDialog(this)) {
                JFileChooser.APPROVE_OPTION -> codeEditor.openFile(selectedFile)
                else -> return
            }
        }
    }

    fun menuSaveAs() {
        JFileChooser(codeEditor.currentFile?.parentFile).apply {
            dialogTitle = "select file to save"
            codeEditor.currentFile?.let {
                selectedFile = it
            }
            when (showSaveDialog(this)) {
                JFileChooser.APPROVE_OPTION -> {
                    codeEditor.saveToFile(selectedFile)
                }
                else -> return
            }
        }
    }

    fun menuRun() {

    }

    fun menuSave() {
        codeEditor.currentFile?.let {
            codeEditor.saveToFile(it)
        }
    }

    fun menuClose() {
        dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
    }
}


fun main(args: Array<String>) {
    // reference: https://docs.oracle.com/javase/tutorial/uiswing/components/index.html
    SwingUtilities.invokeLater {
        CodeEditorJFrame()
    }
}
