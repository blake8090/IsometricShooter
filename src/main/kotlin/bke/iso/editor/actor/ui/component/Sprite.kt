package bke.iso.editor.actor.ui.component

import bke.iso.editor.actor.ui.onChanged
import bke.iso.engine.render.Sprite
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField

fun createSpriteControls(table: Table, skin: Skin, sprite: Sprite) {
    table.add(Label("texture:", skin))
        .left()
        .padRight(5f)
        .padBottom(5f)

    table.add(
        TextField(sprite.texture, skin).apply {
            onChanged {
                println("changed texture to $text")
            }
        })
        .growX()

    table.row()
    table.add(Label("offsetX:", skin))
        .left()
        .padRight(5f)
        .padBottom(5f)

    table.add(
        TextField(sprite.offsetX.toString(), skin).apply {
            onChanged {
                sprite.offsetX = text.toFloatOrNull() ?: 0f
            }
        })
        .growX()

    table.row()
    table.add(Label("offsetY:", skin))
        .left()
        .padRight(5f)
        .padBottom(5f)

    table.add(
        TextField(sprite.offsetY.toString(), skin).apply {
            onChanged {
                sprite.offsetY = text.toFloatOrNull() ?: 0f
            }
        })
        .growX()

    table.row()
    table.add(Label("alpha:", skin))
        .left()
        .padRight(5f)
        .padBottom(5f)

    table.add(
        TextField(sprite.alpha.toString(), skin).apply {
            onChanged {
                println("changed alpha to $text")
                sprite.alpha = text.toFloatOrNull() ?: 0f
            }
        })
        .growX()

    table.row()
    table.add(Label("scale:", skin))
        .left()
        .padRight(5f)
        .padBottom(5f)

    table.add(
        TextField(sprite.scale.toString(), skin).apply {
            onChanged {
                println("changed scale to $text")
                sprite.scale = text.toFloatOrNull() ?: 0f
            }
        })
        .growX()

    table.row()
    table.add(Label("rotation:", skin))
        .left()
        .padRight(5f)
        .padBottom(5f)

    table.add(
        TextField(sprite.rotation.toString(), skin).apply {
            onChanged {
                println("changed scale to $text")
                sprite.rotation = text.toFloatOrNull() ?: 0f
            }
        })
        .growX()
}
