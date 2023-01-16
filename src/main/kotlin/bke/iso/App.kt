package bke.iso

import bke.iso.engine.Engine
import bke.iso.service.ServiceContainer
import bke.iso.service.container
import bke.iso.engine.Game
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.async.KtxAsync
import kotlin.reflect.KClass

class App(
    private val title: String,
    private val gameClass: KClass<out Game>
) : ApplicationAdapter() {
    private lateinit var container: ServiceContainer

    override fun create() {
        KtxAsync.initiate()

        // create container here since LibGdx is now initialized
        container = container {
            inPackage("bke.iso.engine")
            inPackage("bke.iso.game")
        }

        val game = container.get(gameClass)
        container.get<Engine>().start(game)
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
