package com.github.kright.editor

import com.github.kright.interpreter.Output
import java.awt.Color
import java.awt.Insets
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import kotlin.math.min

class ProgramOutput(private val maxOutputLength: Int) : JTextPane() {
    val output: Output

    init {
        margin = Insets(5, 5, 5, 5)
        isEditable = false
        output = Output(
            { msg ->
                SwingUtilities.invokeLater {
                    this.println(msg)
                }
            },
            { err ->
                SwingUtilities.invokeLater {
                    this.error(err)
                }
            }
        )
    }

    private fun appendText(text: String, color: Color) {
        truncateDocumentBeginning((this.text.length + text.length - maxOutputLength))

        val sc = StyleContext.getDefaultStyleContext()
        var aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color)
        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        isEditable = true
        caretPosition = document.length
        setCharacterAttributes(aset, false)
        replaceSelection(truncateStringLength(text))
        isEditable = false
    }

    private fun truncateStringLength(text: String): String {
        if (text.length <= maxOutputLength) {
            return text
        }
        return text.substring(text.length - maxOutputLength)
    }

    private fun truncateDocumentBeginning(symbolsToRemove: Int) {
        if (symbolsToRemove > 0) {
            document.remove(0, min(symbolsToRemove, text.length))
        }
    }

    fun println(text: String) = appendText(text + "\n", Color.black)
    fun error(text: String) = appendText(text + "\n", Color.red)
    fun clear() {
        isEditable = true
        text = ""
        isEditable = false
    }
}