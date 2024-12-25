package bke.iso.editor.actor.ui

import bke.iso.editor.actor.ui.component.createColliderControls
import bke.iso.editor.actor.ui.component.createSpriteControls
import bke.iso.engine.collision.Collider
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener

class ActorInspectorView(private val skin: Skin) {

    private val root = Table()
        .top()
        .padLeft(5f)
        .padRight(5f)

    private lateinit var editorTable: Table

    fun create(): Actor {
        root.background = skin.getDrawable("bg")

        root.add(Label("Component Editor", skin))
            .left()

        root.row()
        editorTable = Table()
        root.add(editorTable)
            .expand()
            .top()
            .left()
            .padTop(5f)

        return root
    }

    fun updateComponent(component: Component) {
        editorTable.clearChildren()

        when (component) {
            is Sprite -> createSpriteControls(editorTable, skin, component)
            is Collider -> createColliderControls(editorTable, skin, component)
        }
    }
}

fun <T : TextField> T.onChanged(action: T.() -> Unit) {
    addListener(object : ChangeListener() {
        override fun changed(event: ChangeEvent, actor: Actor) {
            action.invoke(this@onChanged)
        }
    })
}
