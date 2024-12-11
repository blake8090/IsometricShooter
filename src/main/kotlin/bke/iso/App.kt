package bke.iso

import bke.iso.engine.Game
import bke.iso.engine.GameInfo
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.async.KtxAsync

class App(private val gameInfo: GameInfo) : ApplicationAdapter() {

    val paths = AppPaths(gameInfo)

    private lateinit var game: Game

    fun getConfig() =
        Lwjgl3ApplicationConfiguration().apply {
            setTitle(gameInfo.windowTitle)
            useVsync(false)
            setWindowedMode(1920, 1080)
        }

    override fun create() {
        KtxAsync.initiate()
        game = Game(paths)
        game.start(gameInfo)
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
