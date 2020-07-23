package com.github.kright.editor

import java.awt.Dimension

data class EditorSettings(
    val interpreterPath: String = "build/interpreter/interpreter-0.1/bin/interpreter",
    val maxOutputLength: Int = 10000,
    val code: TextPaneSettings = TextPaneSettings(preferredSize = Dimension(800, 600)),
    val programOutput: TextPaneSettings = TextPaneSettings(preferredSize = Dimension(800, 200))
)

data class TextPaneSettings(
    val preferredSize: Dimension,
    val minimumSize: Dimension = Dimension(200, 100)
)