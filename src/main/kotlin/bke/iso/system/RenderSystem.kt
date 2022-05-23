package bke.iso.system

import bke.iso.asset.AssetService
import bke.iso.MapService
import bke.iso.util.Point
import bke.iso.util.cartesianToIsometric
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class RenderSystem(
    private val assetService: AssetService,
    private val mapService: MapService
) : System() {
    private val batch = SpriteBatch()

    override fun update(deltaTime: Float) {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()
        drawTestMap()
        batch.end()
    }

    // TODO: load and read asset in MapService instead
    private fun drawTestMap() {
        val floorTexture = assetService.getAsset("floor", Texture::class) ?: return
//        val wallTexture = assetService.getTexture("wall") ?: return

        for (y in 5 downTo 0) {
            for (x in 5 downTo 0) {
                val point = Point(
                    x * (mapService.tileWidth.toFloat() / 2f),
                    y * (32f) // TODO: half tile width
                )
                val isoPoint = cartesianToIsometric(point)
                val offsetX = 5f * mapService.tileWidth
                val offsetY = 1 * 64f // TODO: half tile height
                batch.draw(floorTexture, isoPoint.x + offsetX, isoPoint.y + offsetY)
            }
        }
    }
}
