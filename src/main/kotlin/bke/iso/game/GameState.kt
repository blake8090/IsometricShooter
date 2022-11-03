package bke.iso.game

import bke.iso.engine.Renderer
import bke.iso.engine.State
import bke.iso.engine.assets.Assets
import bke.iso.engine.world.entity.CollisionBox
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Sprite
import bke.iso.engine.log
import bke.iso.engine.world.*
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector2

class GameState(
    private val world: World,
    private val assets: Assets,
    private val renderer: Renderer
) : State() {
    private lateinit var player: Entity
    private val speed = 2f

    override fun start() {
        log.debug("building world")

        loadMap()

        player = world.entities.create()
            .addComponent(
                Sprite(
                    "player",
                    Vector2(32f, 0f)
                )
            )
            .addComponent(
                CollisionBox(
                    -0.25f,
                    -0.25f,
                    0.5f,
                    0.5f
                )
            )

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

        player.move(
            (speed * dx) * deltaTime,
            (speed * dy) * deltaTime
        )

        val pos = player.getPos()
        renderer.setCameraPos(pos.x, pos.y)
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
        world.entities.create(x, y)
            .addComponent(Sprite("wall2", Vector2(0f, 16f)))
            .addComponent(CollisionBox(0f, 0f, 1f, 1f))
    }
}
