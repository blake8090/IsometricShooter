package bke.iso

import bke.iso.asset.AssetService
import bke.iso.system.RenderSystem
import bke.iso.system.SystemService
import bke.iso.world.TextureComponent
import bke.iso.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import org.slf4j.LoggerFactory

class Engine {
    private val log = LoggerFactory.getLogger(Engine::class.java)

    private val container = IocContainer()

    private var entityId = 0

    init {
        container.registerFromClassPath("bke.iso")
    }

    fun start() {
        log.info("Starting up")
        container.getService<SystemService>().registerSystems(mutableSetOf(RenderSystem::class))
        loadAssets()

        val world = container.getService<World>()
        world.loadMap("test")
        world.createEntity(listOf(TextureComponent("circle")))
            ?.let { entityId = it }
    }

    fun update(deltaTime: Float) {
        container.getService<SystemService>().update(deltaTime)

        var dx = 0f
        var dy = 0f
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            dy = -1f
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            dy = 1f
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dx = -1f
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dx = 1f
        }

        val world = container.getService<World>()
        val speed = 2f
        world.moveEntity(entityId, (speed * dx) * deltaTime, (speed * dy) * deltaTime)
    }

    fun stop() {
        log.info("Shutting down")
    }

    fun resolveConfig(): Config =
        container.getService<ConfigService>()
            .resolveConfig()

    private fun loadAssets() {
        container.getService<AssetService>().apply {
            // TODO: use globals
            setupAssetLoaders("bke.iso")
            KtxAsync.launch {
                loadAssets("assets")
            }
        }
    }
}
