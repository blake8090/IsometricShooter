package bke.iso.editor.actor.ui.component

import bke.iso.editor.actor.ui.onChanged
import bke.iso.engine.collision.Collider
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextField

fun createColliderControls(table: Table, skin: Skin, collider: Collider) {
    table.add(Label("size", skin))
        .left()

    table.row()
    table.add(Label("x:", skin))
        .left()
        .padRight(5f)
        .padBottom(5f)

    table.add(
        TextField(collider.size.x.toString(), skin).apply {
            onChanged {
                collider.size.x = text.toFloatOrNull() ?: 0f
            }
        })
        .growX()

    table.row()
    table.add(Label("y:", skin))
        .left()
        .padRight(5f)
        .padBottom(5f)

    table.add(
        TextField(collider.size.y.toString(), skin).apply {
            onChanged {
                collider.size.y = text.toFloatOrNull() ?: 0f
            }
        })
        .growX()

    table.row()
    table.add(Label("z:", skin))
        .left()
        .padRight(5f)
        .padBottom(5f)

    table.add(
        TextField(collider.size.z.toString(), skin).apply {
            onChanged {
                collider.size.z = text.toFloatOrNull() ?: 0f
            }
        })
        .growX()

    table.row()
    table.add(Label("offset", skin))
        .left()

    table.row()
    table.add(Label("x:", skin))
        .left()
        .padRight(5f)
        .padBottom(5f)

    table.add(
        TextField(collider.offset.x.toString(), skin).apply {
            onChanged {
                collider.offset.x = text.toFloatOrNull() ?: 0f
            }
        })
        .growX()

    table.row()
    table.add(Label("y:", skin))
        .left()
        .padRight(5f)
        .padBottom(5f)

    table.add(
        TextField(collider.offset.y.toString(), skin).apply {
            onChanged {
                collider.offset.y = text.toFloatOrNull() ?: 0f
            }
        })
        .growX()

    table.row()
    table.add(Label("z:", skin))
        .left()
        .padRight(5f)
        .padBottom(5f)

    table.add(
        TextField(collider.offset.z.toString(), skin).apply {
            onChanged {
                collider.offset.z = text.toFloatOrNull() ?: 0f
            }
        })
        .growX()
}
