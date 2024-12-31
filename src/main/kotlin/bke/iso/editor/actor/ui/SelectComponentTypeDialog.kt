package bke.iso.editor.actor.ui

import bke.iso.engine.ui.util.BorderedTable
import bke.iso.engine.ui.util.onChanged
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.Input
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import kotlin.reflect.KClass

class SelectComponentTypeDialog2(skin: Skin) : Dialog("Add Component", skin) {

    fun create(
        stage: Stage,
        componentTypes: List<KClass<out Component>>,
        action: (KClass<out Component>) -> Unit
    ) {
        text("Select a component:")
        contentTable.row()

        val borderedTable = BorderedTable(skin.getColor("table-border"))

        val content = Table()
        val scrollPane = ScrollPane(content, skin)
        scrollPane.setScrollbarsVisible(true)
        scrollPane.fadeScrollBars = false

        borderedTable.add(scrollPane)
            .grow()
        contentTable.add(borderedTable)
            .grow()

        for (type in componentTypes.sortedBy { s -> s.simpleName }) {
            val name = type.simpleName ?: continue

            val button = TextButton(name, skin)
            button.onChanged {
                action.invoke(type)
                hide()
            }

            content.row()
            content.add(button)
                .growX()
        }

        button("Cancel", false)
        key(Input.Keys.ESCAPE, false)

        show(stage)
    }

    override fun getPrefWidth(): Float {
        return stage.width * 0.25f
    }

    override fun getPrefHeight(): Float {
        return stage.height * 0.25f
    }
}
