package org.chorusmc.chorus.menus.drop.actions.previews

import org.chorusmc.chorus.editor.EditorArea
import org.chorusmc.chorus.menus.coloredtextpreview.ColoredTextPreviewMenu
import org.chorusmc.chorus.menus.coloredtextpreview.previews.ScoreboardPreviewImage
import org.chorusmc.chorus.menus.drop.actions.DropMenuAction
import org.chorusmc.chorus.minecraft.chat.ChatParser
import org.chorusmc.chorus.util.toFlowList
import org.chorusmc.chorus.util.withStyleClass
import eu.iamgio.libfx.timing.WaitingTimer
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.util.Duration

/**
 * @author Gio
 */
class ScoreboardPreview : DropMenuAction() {

    override fun onAction(area: EditorArea, x: Double, y: Double) {
        val title = TextField("Scoreboard")
        val textArea = TextArea(area.selectedText)
        textArea.isCache = false
        WaitingTimer().start({
            val scrollpane = textArea.childrenUnmodifiable[0] as ScrollPane
            scrollpane.isCache = false
            scrollpane.childrenUnmodifiable.forEach {it.isCache = false}
        }, Duration(500.0))
        textArea.prefHeight = 80.0
        textArea.promptText = "Text"
        val menu = ColoredTextPreviewMenu("Scoreboard preview", ScoreboardPreviewImage(title.text, area.selectedText), listOf(title, textArea))
        title.textProperty().addListener {_ ->
            menu.image.flows[0] = ChatParser(title.text, true).toTextFlow().withStyleClass("minecraft-scoreboard-title-preview-flow")
        }
        textArea.textProperty().addListener {_ ->
            val f1 = menu.image.flows[0]
            val lines = textArea.text.split("\n")
            val flows = lines.map {ChatParser(it, true).toTextFlow().withStyleClass((menu.image as ScoreboardPreviewImage).styleClass)}.toMutableList()
            flows.add(0, f1)
            menu.image.flows = flows.toFlowList()
        }
        menu.toFocus = 1
        menu.layoutX = x
        menu.layoutY = y
        menu.show()
    }
}