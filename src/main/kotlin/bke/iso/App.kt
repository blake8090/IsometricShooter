package bke.iso

import bke.iso.engine.Engine
import bke.iso.engine.Game
import bke.iso.engine.asset.AssetService
import bke.iso.engine.render.RenderService
import bke.iso.service.Singleton
import bke.iso.service.Transient
import bke.iso.service.container.ServiceContainer
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ktx.async.KtxAsync
import org.reflections.Reflections
import kotlin.reflect.KClass

class App(
    private val title: String,
    private val gameClass: KClass<out Game>
) : ApplicationAdapter() {

    private lateinit var container: ServiceContainer
    private lateinit var engine: Engine

    override fun create() {
        KtxAsync.initiate()

        // create container here since LibGdx is now initialized
        container = createCache()

        engine = container.get()
        val game = container.get(gameClass)
        engine.start(game)
    }

    override fun dispose() {
        engine.stop()
        container.get<AssetService>().dispose()
        container.get<RenderService>().dispose()
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

    private fun createCache(): ServiceContainer {
        val services = mutableSetOf<KClass<*>>()
        services.addAll(findServices("bke.iso.engine"))
        services.addAll(findServices("bke.iso.game"))

        val cache = ServiceContainer()
        cache.init(services)
        return cache
    }

    private fun findServices(classPath: String): Set<KClass<*>> {
        val services = mutableSetOf<KClass<*>>()

        Reflections(classPath)
            .getTypesAnnotatedWith(Singleton::class.java)
            .map(Class<*>::kotlin)
            .forEach(services::add)

        Reflections(classPath)
            .getTypesAnnotatedWith(Transient::class.java)
            .map(Class<*>::kotlin)
            .forEach(services::add)

        return services
    }
}
