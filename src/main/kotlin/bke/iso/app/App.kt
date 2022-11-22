package bke.iso.app

import bke.iso.app.service.ServiceScanner
import bke.iso.app.service.Services
import bke.iso.engine.Engine
import bke.iso.engine.Game
import ch.qos.logback.classic.Level
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.async.KtxAsync

class App(private val game: Game) : ApplicationAdapter() {
    private val services = Services()

    init {
        configureLogback(Level.TRACE)
        ServiceScanner()
            .scanClasspath("bke.iso")
            .forEach { javaClass -> services.register(javaClass.kotlin) }
    }

    override fun create() {
        KtxAsync.initiate()
        services.get<Engine>().start(game)
    }

    override fun render() {
        services.get<Engine>().update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        services.get<Engine>().stop()
    }

    fun buildConfig(): Lwjgl3ApplicationConfiguration {
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle(game.windowTitle)
        config.useVsync(false)
        // TODO: load this from application config
        config.setWindowedMode(1920, 1080)
        return config
    }
}
