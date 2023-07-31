package bke.iso.v2

import bke.iso.v2.engine.Game
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.async.KtxAsync

class App(title: String) : ApplicationAdapter() {
    val game = Game()

    val config: Lwjgl3ApplicationConfiguration by lazy {
        // TODO: load this from application config
        Lwjgl3ApplicationConfiguration().apply {
            setTitle(title)
            useVsync(false)
            setWindowedMode(2560, 1440)
        }
    }

    override fun create() {
        KtxAsync.initiate()
        game.start()
    }

    override fun render() {
        game.update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        game.stop()
    }
}