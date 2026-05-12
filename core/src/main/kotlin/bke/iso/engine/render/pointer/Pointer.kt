package bke.iso.engine.render.pointer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2

abstract class Pointer {

    val pos = Vector2()

    var visible: Boolean = true
        protected set

    abstract fun create()

    abstract fun show()

    abstract fun hide()

    open fun update(deltaTime: Float) {
        pos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
    }

    abstract fun draw(batch: PolygonSpriteBatch, screenPos: Vector2)
}
