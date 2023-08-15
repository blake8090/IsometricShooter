package bke.iso.engine.render

import bke.iso.engine.asset.Assets
import bke.iso.engine.math.toVector2
import bke.iso.engine.math.toWorld
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import mu.KotlinLogging

class Cursor(
    private val assets: Assets,
    private val camera: Camera
) {

    private val log = KotlinLogging.logger {}

    val pos: Vector2
        get() = camera.unproject(
            Vector3(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
        ).toVector2()

    val worldPos: Vector3
        get() = toWorld(pos)

    fun set(textureName: String) {
        val texture = assets.get<Texture>(textureName)
        val xHotspot = texture.width / 2
        val yHotspot = texture.height / 2
        texture.textureData.prepare()
        val pixmap = texture.textureData.consumePixmap()
        Gdx.graphics.setCursor(Gdx.graphics.newCursor(pixmap, xHotspot, yHotspot))
        pixmap.dispose()
    }

    fun reset() {
        log.debug { "reset cursor" }
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
    }
}
