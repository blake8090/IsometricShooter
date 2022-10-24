package bke.iso.v2.app

import bke.iso.v2.app.service.ServiceScanner
import bke.iso.v2.app.service.Services
import bke.iso.v2.engine.Engine
import bke.iso.v2.engine.GameData
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.async.KtxAsync

class App(private val gameData: GameData) : ApplicationAdapter() {
    private val services = Services()

    init {
        ServiceScanner()
            .scanClasspath("bke.iso")
            .forEach { javaClass -> services.register(javaClass.kotlin) }
    }

    override fun create() {
        KtxAsync.initiate()
        services.get<Engine>().start(gameData)
    }

    override fun render() {
        services.get<Engine>().update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        services.get<Engine>().stop()
    }

    fun buildConfig(): Lwjgl3ApplicationConfiguration {
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle(gameData.windowTitle)
        config.useVsync(false)
        // TODO: load this from application config
        config.setWindowedMode(800, 600)
        return config
    }
}
