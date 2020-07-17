package com.github.kright.editor

import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.WindowEvent
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.SwingUtilities

class CodeEditorJFrame : JFrame("code editor") {
    val menuBar: EditorMenuBar
    val codeEditor: CodeEditor
    val programOutput: ProgramOutput

    private val lock = Object()

    private var interpreterWrapper: InterpreterWrapper? = null
        get() = synchronized(lock) { field }
        set(value) = synchronized(lock) {
            val isRunningProgram = value != null && !value.wasStopped

            SwingUtilities.invokeLater {
                menuBar.setHasRunningProgram(isRunningProgram)
            }

            if (field != value) {
                field?.stop()
            }
            field = if (isRunningProgram) value else null
        }

    init {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        menuBar = EditorMenuBar(this)
        codeEditor = CodeEditor(this)

        programOutput = ProgramOutput().apply {
            println("program output:")
        }

        layout = GridBagLayout()
        val codeEditorConstraints = GridBagConstraints().apply {
            fill = GridBagConstraints.BOTH
            weighty = 1.0
            weightx = 1.0
            gridy = 0
        }

        val programOutputConstraints = GridBagConstraints().apply {
            fill = GridBagConstraints.BOTH
            weighty = 0.25
            weightx = 1.0
            gridy = 1
        }

        minimumSize = Dimension(400, 200)

        jMenuBar = menuBar
        contentPane.add(JScrollPane(codeEditor), codeEditorConstraints)
        contentPane.add(JScrollPane(programOutput), programOutputConstraints)

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

    fun menuSaveAs(): File? {
        JFileChooser(codeEditor.currentFile?.parentFile).apply {
            dialogTitle = "select file to save"
            codeEditor.currentFile?.let {
                selectedFile = it
            }
            return when (showSaveDialog(this)) {
                JFileChooser.APPROVE_OPTION -> codeEditor.saveToFile(selectedFile)
                else -> null
            }
        }
    }

    fun menuRunOrStop() {
        val savedFile =
            if (codeEditor.currentFile == null) {
                menuSaveAs()
            } else {
                menuSave()
            }

        if (savedFile == null) {
            return
        }

        synchronized(lock) {
            if (interpreterWrapper != null) {
                interpreterWrapper = null
                return
            }
        }

        runFile(savedFile)
    }

    private fun runFile(file: File) {
        programOutput.clear()

        interpreterWrapper = InterpreterWrapper.runFile(
            File("build/interpreter/interpreter-0.1/bin/interpreter"),
            file ,
            programOutput.output
        ) { wrapper ->
            synchronized(lock) {
                // if wrapper isn't changed
                if (interpreterWrapper == wrapper) {
                    interpreterWrapper = null
                }
            }
        }
    }

    fun menuSave(): File? {
        codeEditor.currentFile?.let {
            return codeEditor.saveToFile(it)
        }
        return null
    }

    fun menuClose() {
        dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
    }
}
