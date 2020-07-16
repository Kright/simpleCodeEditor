package com.github.kright.editor

import javax.swing.JButton
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem

class EditorMenuBar(private val editor: CodeEditorJFrame) : JMenuBar() {

    val saveItem: JMenuItem

    init {
        // http://java-online.ru/swing-menu.xhtml
        add(JMenu("File").apply {
            add(makeAction("open") { editor.menuOpenFile() })
            saveItem = add(makeAction("save") { editor.menuSave() }).apply {
                isVisible = false
            }
            add(makeAction("save as") { editor.menuSaveAs() })
            addSeparator()
            add(makeAction("exit") { editor.menuClose() })
        })

        add(JButton("run").apply {
            action = makeAction("run") { editor.menuRun() }
        })
    }
}