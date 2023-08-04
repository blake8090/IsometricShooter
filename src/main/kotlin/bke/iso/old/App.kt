package bke.iso.old

import bke.iso.old.engine.Game
import bke.iso.old.service.Service
import bke.iso.old.service.ServiceContainer
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.async.KtxAsync
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class App(
    private val title: String,
    private val gameClass: KClass<out Game>
) : ApplicationAdapter() {

    private val container = ServiceContainer()
    private lateinit var engine: bke.iso.old.engine.Engine

    override fun create() {
        KtxAsync.initiate()

        container.register(findServices("bke.iso.old.engine") + findServices("bke.iso.old.game"))

        engine = container.get()
        val game = container.get(gameClass)
        engine.start(game)
    }

    override fun dispose() {
        engine.stop()
        container.dispose()
    }

    override fun render() {
        engine.update(Gdx.graphics.deltaTime)
    }

    fun buildConfig(): Lwjgl3ApplicationConfiguration =
        Lwjgl3ApplicationConfiguration().apply {
            setTitle(title)
            useVsync(false)
            // TODO: load this from application config
            setWindowedMode(1920, 1080)
        }

    private fun findServices(classPath: String): Set<KClass<out Service>> =
        Reflections(classPath)
            .getSubTypesOf(Service::class.java)
            .map(Class<out Service>::kotlin)
            .filter { it.primaryConstructor != null }
            .toSet()
}
