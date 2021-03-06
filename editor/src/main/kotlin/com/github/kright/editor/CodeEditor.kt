package com.github.kright.editor

import java.io.File
import javax.swing.JTextPane

class CodeEditor(private val editor: CodeEditorJFrame) : JTextPane() {
    var currentFile: File? = null
        set(value) {
            field = value
            val isValid = value?.exists() == true
            editor.setHasSaveOption(isValid)
        }

    fun openFile(f: File) {
        currentFile = f
        text = f.readText()
    }

    fun saveToFile(f: File): File? {
        currentFile = f
        f.writeText(text)
        return f
    }
}