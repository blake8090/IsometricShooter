package bke.iso

import bke.iso.engine.Game
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.async.KtxAsync

class App(title: String) : ApplicationAdapter() {
    private lateinit var game: Game

    val config: Lwjgl3ApplicationConfiguration by lazy {
        // TODO: load this from application config
        Lwjgl3ApplicationConfiguration().apply {
            setTitle(title)
            // TODO: fix vsync jitter, investigate using fixed time step
            useVsync(false)
            setWindowedMode(1920, 1080)
        }
    }

    override fun create() {
        KtxAsync.initiate()
        game = Game()
        game.start()
    }

    override fun render() {
        game.update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        game.stop()
    }

    override fun resize(width: Int, height: Int) {
        game.resize(width, height)
    }
}
