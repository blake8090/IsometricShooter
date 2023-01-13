package bke.iso

import bke.iso.engine.Engine
import bke.iso.service.ServiceContainer
import bke.iso.service.container
import bke.iso.engine.Game
import bke.iso.engine.event.EventService
import bke.iso.engine.system.SystemService
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

        // TODO: have a Service interface with post init methods
        container.get<EventService>().start()
        container.get<SystemService>().start()

        val game = container.getProvider<Game>().get(gameClass)
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
