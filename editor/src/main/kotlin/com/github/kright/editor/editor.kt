package com.github.kright.editor

import javax.swing.SwingUtilities


fun main(args: Array<String>) {
    // reference: https://docs.oracle.com/javase/tutorial/uiswing/components/index.html
    SwingUtilities.invokeLater {
        CodeEditorJFrame()
    }
}
