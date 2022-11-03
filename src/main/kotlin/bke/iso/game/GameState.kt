package bke.iso.game

import bke.iso.engine.Input
import bke.iso.engine.Renderer
import bke.iso.engine.State
import bke.iso.engine.assets.Assets
import bke.iso.engine.world.entity.CollisionBox
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Sprite
import bke.iso.engine.log
import bke.iso.engine.world.*
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.Vector2
import kotlin.system.measureTimeMillis

class GameState(
    private val world: World,
    private val assets: Assets,
    private val renderer: Renderer,
    private val input: Input
) : State() {
    private lateinit var player: Entity
    private val speed = 2f

    override fun start() {
        log.debug("building world")

        val loadingTime = measureTimeMillis {
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
        }
        log.debug("built world in $loadingTime ms")

        log.debug("binding actions")
        input.bind(Keys.LEFT, "moveLeft")
        input.bind(Keys.RIGHT, "moveRight")
        input.bind(Keys.UP, "moveUp")
        input.bind(Keys.DOWN, "moveDown")
    }

    override fun update(deltaTime: Float) {
        var dx = 0f
        var dy = 0f

        input.onAction("moveLeft") { axis ->
            dx = axis * -1
        }

        input.onAction("moveRight") { axis ->
            dx = axis
        }

        input.onAction("moveUp") { axis ->
            dy = axis
        }

        input.onAction("moveDown") { axis ->
            dy = axis * -1
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
