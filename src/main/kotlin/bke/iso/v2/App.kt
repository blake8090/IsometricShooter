package bke.iso.v2

import bke.iso.service.ServiceContainer
import bke.iso.service.container
import bke.iso.v2.engine.Engine
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.async.KtxAsync

class App(private val title: String) : ApplicationAdapter() {
    private lateinit var container: ServiceContainer

    override fun create() {
        KtxAsync.initiate()

        // create container here since LibGdx is now initialized
        container = container {
            inPackage("bke.iso.v2.engine")
        }

        container.get<Engine>().start()
    }

    override fun dispose() {
        container.get<Engine>().stop()
    }

    override fun render() {
        container.get<Engine>().update(Gdx.graphics.deltaTime)
    }

    fun buildConfig(): Lwjgl3ApplicationConfiguration =
        Lwjgl3ApplicationConfiguration().apply {
            setTitle(title)
            useVsync(false)
            // TODO: load this from application config
            setWindowedMode(1920, 1080)
        }
}
