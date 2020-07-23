package com.github.kright.editor

import java.awt.event.WindowEvent
import java.io.File
import javax.swing.*

class CodeEditorJFrame : JFrame("code editor") {
    private val menuBar: EditorMenuBar
    private val codeEditor: CodeEditor
    private val programOutput: ProgramOutput
    private val settings = EditorSettings()
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

        programOutput = ProgramOutput(maxOutputLength = settings.maxOutputLength).apply {
            println("program output:")
        }

        val splitPane = JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            JScrollPane(codeEditor).apply {
                minimumSize = settings.code.minimumSize
                preferredSize = settings.code.preferredSize
            },
            JScrollPane(programOutput).apply {
                minimumSize = settings.programOutput.minimumSize
                preferredSize = settings.programOutput.preferredSize
            }
        )

        splitPane.apply {
            resizeWeight = 1.0
            isOneTouchExpandable = true
        }

        jMenuBar = menuBar
        contentPane.add(splitPane)

        pack()
        isVisible = true
    }

    internal fun menuOpenFile() {
        JFileChooser(codeEditor.currentFile?.parentFile).apply {
            dialogTitle = "select file to open"
            when (showOpenDialog(this)) {
                JFileChooser.APPROVE_OPTION -> codeEditor.openFile(selectedFile)
                else -> return
            }
        }
    }

    internal fun menuSaveAs(): File? {
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

    internal fun menuRunOrStop() {
        synchronized(lock) {
            if (interpreterWrapper != null) {
                interpreterWrapper = null
                return
            }
        }

        runCode(codeEditor.text)
    }

    private fun runCode(code: String) {
        programOutput.clear()

        interpreterWrapper = InterpreterWrapper.runCode(
            File(settings.interpreterPath),
            code,
            programOutput.output
        ) { wrapper ->
            synchronized(lock) {
                // if wrapper wasn't changed
                if (interpreterWrapper == wrapper) {
                    interpreterWrapper = null
                }
            }
        }
    }

    internal fun menuSave(): File? {
        codeEditor.currentFile?.let {
            return codeEditor.saveToFile(it)
        }
        return null
    }

    internal fun menuClose() {
        dispatchEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSING))
    }

    internal fun setHasSaveOption(hasOption: Boolean) =
        menuBar.setHasSaveOption(hasOption)
}
