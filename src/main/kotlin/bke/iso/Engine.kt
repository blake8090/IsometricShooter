package bke.iso

import bke.iso.asset.AssetService
import bke.iso.di.ServiceContainer
import bke.iso.render.Renderer
import bke.iso.system.SystemService
import bke.iso.world.entity.Entity
import bke.iso.world.entity.TextureComponent
import bke.iso.world.World
import bke.iso.world.entity.PositionComponent
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import org.slf4j.LoggerFactory

class Engine {
    private val log = LoggerFactory.getLogger(Engine::class.java)

    private val container = ServiceContainer()

    private lateinit var player: Entity

    init {
        container.registerFromClassPath("bke.iso")
    }

    fun start() {
        log.info("Starting up")
        loadAssets()

        val world = container.getService<World>()
        world.loadMap("test")
        player = world.createEntity(TextureComponent("player"))
    }

    fun update(deltaTime: Float) {
        container.getService<SystemService>().update(deltaTime)

        updatePlayer(deltaTime)
        player.findComponent<PositionComponent>()?.let { positionComponent ->
            val world = container.getService<World>()
            val pos = world.unitConverter.worldToScreen(positionComponent)
            container.getService<Renderer>().setCameraPos(pos.x, pos.y)
        }

        container.getService<Renderer>().render()
    }

    private fun updatePlayer(deltaTime: Float) {
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

        val speed = 2f
        player.move((speed * dx) * deltaTime, (speed * dy) * deltaTime)
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
            setupAssetLoadersInPackage("bke.iso")
            KtxAsync.launch {
                loadAssets("assets")
            }
        }
    }
}
