package bke.iso.editor.actor.ui

import bke.iso.editor.ui.color
import bke.iso.engine.collision.Collider
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener

class ActorInspectorView(private val skin: Skin) {

    private val root = Table()
        .top()
        .padLeft(5f)
        .padRight(5f)


    private lateinit var editorTable: Table

    fun create(): Actor {
        setup()

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

    private fun setup() {
        skin.add("actorInspector", TextField.TextFieldStyle().apply {
            font = skin.getFont("default")
            fontColor = Color.WHITE
            focusedFontColor = Color.WHITE

            background = skin.newDrawable("pixel", Color.BLACK)
            focusedBackground = skin.newDrawable("pixel", Color.BLACK)

            cursor = skin.newDrawable("pixel", color(50, 158, 168))
            selection = skin.newDrawable("pixel", color(50, 158, 168))
        })

        skin.add("actorInspector", TextButton.TextButtonStyle().apply {
            font = skin.getFont("default")
            down = skin.newDrawable("pixel", color(43, 103, 161))
            over = skin.newDrawable("pixel", color(34, 84, 133))
        })
    }

    fun updateComponent(component: Component) {
        editorTable.clearChildren()

        when (component) {
            is Sprite -> createSpriteControls(component)
            is Collider -> createColliderControls(component)
        }
    }

    private fun createSpriteControls(sprite: Sprite) {
        editorTable.add(Label("texture:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)

        editorTable.add(
            TextField(sprite.texture, skin, "actorInspector").apply {
                onChanged {
                    println("changed texture to $text")
                }
            })
            .growX()

        editorTable.row()
        editorTable.add(Label("offsetX:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)

        editorTable.add(
            TextField(sprite.offsetX.toString(), skin, "actorInspector").apply {
                onChanged {
                    sprite.offsetX = text.toFloatOrNull() ?: 0f
                }
            })
            .growX()

        editorTable.row()
        editorTable.add(Label("offsetY:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)

        editorTable.add(
            TextField(sprite.offsetY.toString(), skin, "actorInspector").apply {
                onChanged {
                    sprite.offsetY = text.toFloatOrNull() ?: 0f
                }
            })
            .growX()

        editorTable.row()
        editorTable.add(Label("alpha:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)

        editorTable.add(
            TextField(sprite.alpha.toString(), skin, "actorInspector").apply {
                onChanged {
                    println("changed alpha to $text")
                    sprite.alpha = text.toFloatOrNull() ?: 0f
                }
            })
            .growX()

        editorTable.row()
        editorTable.add(Label("scale:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)

        editorTable.add(
            TextField(sprite.scale.toString(), skin, "actorInspector").apply {
                onChanged {
                    println("changed scale to $text")
                    sprite.scale = text.toFloatOrNull() ?: 0f
                }
            })
            .growX()

        editorTable.row()
        editorTable.add(Label("rotation:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)

        editorTable.add(
            TextField(sprite.rotation.toString(), skin, "actorInspector").apply {
                onChanged {
                    println("changed scale to $text")
                    sprite.rotation = text.toFloatOrNull() ?: 0f
                }
            })
            .growX()
    }

    private fun createColliderControls(collider: Collider) {
        editorTable.add(Label("size", skin))
            .left()

        editorTable.row()
        editorTable.add(Label("x:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)

        editorTable.add(
            TextField(collider.size.x.toString(), skin, "actorInspector").apply {
                onChanged {
                    collider.size.x = text.toFloatOrNull() ?: 0f
                }
            })
            .growX()

        editorTable.row()
        editorTable.add(Label("y:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)

        editorTable.add(
            TextField(collider.size.y.toString(), skin, "actorInspector").apply {
                onChanged {
                    collider.size.y = text.toFloatOrNull() ?: 0f
                }
            })
            .growX()

        editorTable.row()
        editorTable.add(Label("z:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)

        editorTable.add(
            TextField(collider.size.z.toString(), skin, "actorInspector").apply {
                onChanged {
                    collider.size.z = text.toFloatOrNull() ?: 0f
                }
            })
            .growX()

        editorTable.row()
        editorTable.add(Label("offset", skin))
            .left()

        editorTable.row()
        editorTable.add(Label("x:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)

        editorTable.add(
            TextField(collider.offset.x.toString(), skin, "actorInspector").apply {
                onChanged {
                    collider.offset.x = text.toFloatOrNull() ?: 0f
                }
            })
            .growX()

        editorTable.row()
        editorTable.add(Label("y:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)

        editorTable.add(
            TextField(collider.offset.y.toString(), skin, "actorInspector").apply {
                onChanged {
                    collider.offset.y = text.toFloatOrNull() ?: 0f
                }
            })
            .growX()

        editorTable.row()
        editorTable.add(Label("z:", skin))
            .left()
            .padRight(5f)
            .padBottom(5f)

        editorTable.add(
            TextField(collider.offset.z.toString(), skin, "actorInspector").apply {
                onChanged {
                    collider.offset.z = text.toFloatOrNull() ?: 0f
                }
            })
            .growX()
    }
}

fun <T : TextField> T.onChanged(action: T.() -> Unit) {
    addListener(object : ChangeListener() {
        override fun changed(event: ChangeEvent, actor: Actor) {
            action.invoke(this@onChanged)
        }
    })
}
