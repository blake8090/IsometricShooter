package bke.iso.system

import bke.iso.AssetService
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch

class RenderSystem(private val assetService: AssetService) : System() {
    private val batch = SpriteBatch()

    override fun update(deltaTime: Float) {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()
        assetService.getTexture("test")
            ?.let { texture -> batch.draw(texture, 0f, 0f) }
        batch.end()
    }
}
