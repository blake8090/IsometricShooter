package bke.iso.app

import bke.iso.app.service.ServiceScanner
import bke.iso.app.service.Services
import bke.iso.engine.Engine
import bke.iso.engine.Game
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.async.KtxAsync
import kotlin.reflect.KClass

class App(gameClass: KClass<out Game>) : ApplicationAdapter() {
    private val services = Services()
    private val game: Game

    init {
        ServiceScanner()
            .scanClasspath("bke.iso")
            .forEach { javaClass -> services.register(javaClass.kotlin) }
        game = services.createInstance(gameClass)
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
