package bke.iso.engine.render

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.badlogic.gdx.math.Vector2
import mu.KotlinLogging

abstract class CustomCursor {

    private val log = KotlinLogging.logger {}

    var enabled = false
        private set

    fun enable() {
        val pixmap = makePixel(Color.CLEAR)
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pixmap, 0, 0))
        pixmap.dispose()
        enabled = true
        log.debug { "enabled custom cursor" }
    }

    fun disable() {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
        enabled = false
        log.debug { "reset cursor" }
    }

    abstract fun draw(batch: PolygonSpriteBatch, cursorPos: Vector2)

    open fun dispose() {}
}
