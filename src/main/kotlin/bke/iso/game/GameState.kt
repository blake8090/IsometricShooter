package bke.iso.game

import bke.iso.engine.Renderer
import bke.iso.engine.State
import bke.iso.engine.assets.Assets
import bke.iso.engine.log
import bke.iso.engine.world.Sprite
import bke.iso.engine.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import java.util.UUID

class GameState(
    private val world: World,
    private val assets: Assets,
    private val renderer: Renderer
) : State() {
    private lateinit var player: UUID
    private val speed = 2f

    override fun start() {
        log.debug("building world")

        loadMap()

        // TODO: add components and position to create method for convenience
        player = world.entities.create()
        world.entities.addComponent(player, Sprite("player"))
        world.entities.setPos(player, 3f, 2f)

        log.debug("finished!")
    }

    override fun update(deltaTime: Float) {
        var dx = 0f
        var dy = 0f

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            dy = 1f
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            dy = -1f
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dx = -1f
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dx = 1f
        }

        world.entities.move(
            player,
            (speed * dx) * deltaTime,
            (speed * dy) * deltaTime
        )

        world.entities.getPos(player)
            ?.let { renderer.setCameraPos(it.x, it.y) }
    }

    private fun loadMap() {
        val mapData = assets.get<MapData>("test")
            ?: throw IllegalArgumentException("expected map asset")

        mapData.tiles.forEach { (location, tile) ->
            world.setTile(tile, location.x, location.y)
        }

        mapData.walls.forEach { location ->
            createWall(location.x.toFloat(), location.y.toFloat())
        }
    }

    private fun createWall(x: Float, y: Float) {
        world.entities.apply {
            val wall = create()
            setPos(wall, x, y)
            addComponent(wall, Sprite("wall2"))
        }
    }
}
