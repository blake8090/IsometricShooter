package bke.iso.system

import bke.iso.asset.AssetService
import bke.iso.map.MapCoord
import bke.iso.map.MapService
import bke.iso.map.Tile
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2

class RenderSystem(
    private val assetService: AssetService,
    private val mapService: MapService
) : System() {
    private val batch = SpriteBatch()

    override fun update(deltaTime: Float) {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()
        drawMap()
        batch.end()
    }

    private fun drawMap() {
        mapService.forEachTile { coord, tile ->
            drawTile(tile, coord)
        }
    }

    private fun drawTile(tile: Tile, coord: MapCoord) {
        val texture = assetService.getAsset(tile.texture, Texture::class) ?: return
        // swap and invert for iso projection where x -> bottom right and y -> bottom left (in terms of screen pos)
        val isoCord = MapCoord(coord.y * -1, coord.x * -1)
        val screenPos = Vector2(
            (isoCord.x - isoCord.y) * (mapService.tileWidth / 2).toFloat(),
            (isoCord.x + isoCord.y) * (mapService.tileHeight / 2).toFloat()
        )
        batch.draw(texture, screenPos.x + 500, screenPos.y + 500)
    }
}
