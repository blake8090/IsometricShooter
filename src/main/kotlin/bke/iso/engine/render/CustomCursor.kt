package bke.iso.engine.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2

abstract class CustomCursor {
    /**
     * Returns the **raw** position of the cursor; not the un-projected position.
     */
    open fun getPos(): Vector2 = Vector2(
        Gdx.input.x.toFloat(),
        Gdx.input.y.toFloat()
    )

    abstract fun create()

    abstract fun update(deltaTime: Float)

    abstract fun draw(batch: PolygonSpriteBatch, unProjectedPos: Vector2)
}
