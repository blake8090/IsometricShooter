package bke.iso.engine.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2

class MousePointer : Pointer() {

    override fun create() {
    }

    override fun show() {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
    }

    override fun hide() {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None)
    }

    override fun update(deltaTime: Float) {
        pos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
    }

    override fun draw(batch: PolygonSpriteBatch, screenPos: Vector2) {
    }
}
