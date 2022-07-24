package bke.iso.game

import bke.iso.engine.State
import bke.iso.engine.asset.AssetService
import bke.iso.engine.render.Renderer
import bke.iso.engine.util.getLogger
import bke.iso.engine.world.World
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.PositionComponent
import bke.iso.engine.world.entity.TextureComponent
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import kotlin.system.measureTimeMillis

class GameState(
    private val world: World,
    private val assetService: AssetService,
    private val renderer: Renderer
) : State() {
    private val log = getLogger()

    private lateinit var player: Entity

    override fun start() {
        log.info("Starting game state")
        load()
        player = world.createEntity(TextureComponent("player"))
    }

    override fun stop() {
    }

    override fun input(deltaTime: Float) {
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

    override fun update(deltaTime: Float) {
        player.findComponent<PositionComponent>()?.let { positionComponent ->
            val pos = world.unitConverter.worldToScreen(positionComponent)
            renderer.setCameraPos(pos.x, pos.y)
        }
    }

    private fun load() {
        val assetLoadingTime = measureTimeMillis {
            assetService.loadAssets("assets")
        }
        log.info("Loaded assets in $assetLoadingTime ms")

        val mapLoadingTime = measureTimeMillis {
            world.loadMap("test")
        }
        log.info("Loaded map in $mapLoadingTime ms")
    }
}
