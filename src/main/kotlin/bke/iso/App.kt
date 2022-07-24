package bke.iso

import bke.iso.engine.ConfigService
import bke.iso.engine.Engine
import bke.iso.engine.GameInfo
import bke.iso.engine.di.ServiceContainer
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.async.KtxAsync

class App : ApplicationAdapter() {
    private val container = ServiceContainer("bke.iso")
    private val engine = container.getService<Engine>()

    val config: Lwjgl3ApplicationConfiguration by lazy { buildConfig() }

    override fun create() {
        KtxAsync.initiate()
        engine.start()
    }

    override fun render() {
        engine.update(Gdx.graphics.deltaTime)
    }

    override fun dispose() {
        engine.stop()
    }

    private fun buildConfig(): Lwjgl3ApplicationConfiguration {
        val config = container.getService<ConfigService>().resolveConfig()
        val gameInfo = container.getService<GameInfo>()

        val appConfig = Lwjgl3ApplicationConfiguration()
        appConfig.setTitle(gameInfo.windowTitle)

        if (!config.fullScreen) {
            appConfig.setWindowedMode(config.width, config.height)
        } else {
            TODO("fullscreen mode not implemented yet")
        }

        return appConfig
    }
}
