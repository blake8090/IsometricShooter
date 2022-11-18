package bke.iso.game

import bke.iso.engine.*
import bke.iso.engine.assets.Assets
import bke.iso.engine.entity.Entities
import bke.iso.engine.entity.Entity
import bke.iso.engine.entity.Sprite
import bke.iso.engine.input.Input
import bke.iso.engine.input.InputState
import bke.iso.engine.input.KeyBinding
import bke.iso.engine.input.MouseBinding
import bke.iso.engine.physics.Collision
import bke.iso.engine.physics.CollisionBounds
import bke.iso.engine.physics.CollisionEvent
import bke.iso.engine.physics.Velocity
import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.math.Vector2
import kotlin.system.measureTimeMillis

class GameState(
    private val entities: Entities,
    private val tiles: Tiles,
    private val assets: Assets,
    private val renderer: Renderer,
    private val input: Input
) : State() {
    private lateinit var player: Entity
    private val playerWalkSpeed = 4f
    private val playerRunSpeed = 8f

    override fun start() {
        renderer.mouseCursor = Sprite("cursor", Vector2(16f, 16f))

        log.debug("building world")
        val loadingTime = measureTimeMillis {
            loadMap()
            player = createPlayer()
            log.debug("Player entity id: ${player.id}")
        }
        log.debug("built world in $loadingTime ms")

        log.debug("binding actions")
        input.bind("toggleDebug", KeyBinding(Keys.M, InputState.PRESSED))
        input.bind("moveLeft", KeyBinding(Keys.LEFT, InputState.DOWN, true))
        input.bind("moveRight", KeyBinding(Keys.RIGHT, InputState.DOWN))
        input.bind("moveUp", KeyBinding(Keys.UP, InputState.DOWN))
        input.bind("moveDown", KeyBinding(Keys.DOWN, InputState.DOWN, true))
        input.bind("run", KeyBinding(Keys.SHIFT_LEFT, InputState.DOWN))
        input.bind("shoot", MouseBinding(Buttons.LEFT, InputState.PRESSED))
    }

    override fun update(deltaTime: Float) {
        input.onAction("toggleDebug") {
            renderer.toggleDebug()
        }

        input.onAction("shoot") {
            log.debug("shoot action")
        }

        entities.withComponent(CollisionEvent::class) { entity, collisionEvent ->
            log.trace("entity $entity has collided with ${collisionEvent.ids.size} ids")
        }
        updatePlayer()
    }

    private fun updatePlayer() {
        renderer.setCameraPos(player.getPos())

        val dx = input.poll("moveLeft", "moveRight")
        val dy = input.poll("moveUp", "moveDown")

        var speed = playerWalkSpeed
        input.onAction("run") {
            speed = playerRunSpeed
        }
        player.addComponent(Velocity(dx * speed, dy * speed))
    }

    private fun loadMap() {
        val mapData = assets.get<MapData>("test")
            ?: throw IllegalArgumentException("expected map asset")

        mapData.tiles.forEach { (location, tile) ->
            tiles.setTile(tile, location)
        }

        mapData.walls.forEach { location ->
            createWall(location.x.toFloat(), location.y.toFloat())
        }
    }

    private fun createWall(x: Float, y: Float) {
        entities.create(x, y)
            .addComponent(Sprite("wall3", Vector2(0f, 16f)))
            .addComponent(
                Collision(
                    CollisionBounds(
                        1f,
                        1f
                    ),
                    true
                )
            )
    }

    private fun createPlayer() =
        entities.create(1f, 0f)
            .addComponent(
                Sprite(
                    "player",
                    Vector2(32f, 0f)
                )
            )
            .addComponent(
                Collision(
                    CollisionBounds(
                        0.5f,
                        0.5f,
                        Vector2(-0.25f, -0.25f)
                    )
                )
            )
}
