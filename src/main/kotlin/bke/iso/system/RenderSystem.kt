package bke.iso.system

import bke.iso.asset.AssetService
import bke.iso.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class RenderSystem(
    private val assetService: AssetService,
    private val world: World
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
        world.forEachTile { location, tile ->
            val texture = assetService.getAsset(tile.texture, Texture::class) ?: return@forEachTile
            val pos = world.worldToScreen(location)
            batch.draw(texture, pos.x + 500, pos.y + 600)
        }
    }
}
