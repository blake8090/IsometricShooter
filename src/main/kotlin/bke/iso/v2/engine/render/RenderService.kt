package bke.iso.v2.engine.render

import bke.iso.service.Singleton
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2

@Singleton
class RenderService {

    private val batch = SpriteBatch()
    private val camera = OrthographicCamera(1920f, 1080f)

    fun setCameraPos(pos: Vector2) {
        camera.position.x = pos.x
        camera.position.y = pos.y
    }

    fun render() {
        Gdx.gl.glClearColor(0f, 0f, 255f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        batch.projectionMatrix = camera.combined
    }
}