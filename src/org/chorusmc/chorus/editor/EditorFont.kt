package org.chorusmc.chorus.editor

import org.chorusmc.chorus.Chorus

/**
 * @author Giorgio Garofalo
 */
enum class EditorFont(val fontName: String, vararg val fontFileNames: String) {

    DEFAULT(""),
    INCONSOLATA("Inconsolata", "Inconsolata-Regular.ttf", "Inconsolata-Bold.ttf");

    fun load() = fontFileNames.forEach { Chorus.getInstance().loadFont(it) }
}