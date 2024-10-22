package bke.iso.engine.render.pointer

import bke.iso.engine.math.toWorld
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3

class PointerRenderer(
    private val camera: Camera,
    private val batch: PolygonSpriteBatch // TODO: needs to be SpriteBatch instead
) {

    private var pointer: Pointer = MousePointer()

    val visible: Boolean
        get() = pointer.visible

    val pos: Vector2
        get() =
            camera
                .unproject(Vector3(pointer.pos, 0f))
                .toVector2()

    val worldPos: Vector3
        get() = toWorld(pos)

    fun set(newPointer: Pointer) {
        pointer.hide()
        newPointer.create()
        newPointer.show()
        pointer = newPointer
    }

    fun update(deltaTime: Float) {
        pointer.update(deltaTime)
    }

    fun draw() {
        pointer.draw(batch, pos)
    }
}

private fun Vector3.toVector2(): Vector2 =
    Vector2(x, y)
