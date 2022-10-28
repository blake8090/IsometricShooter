package bke.iso.v2.game

import bke.iso.v2.engine.State
import bke.iso.v2.engine.log
import bke.iso.v2.engine.world.Sprite
import bke.iso.v2.engine.world.Tile
import bke.iso.v2.engine.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import java.util.UUID

class GameState(private val world: World) : State() {
    private lateinit var player: UUID
    private val speed = 2f

    override fun start() {
        log.debug("building world")

        val tile = Tile("floor")
        for (y in 0..5) {
            for (x in 0..10) {
                world.setTile(tile, x, y)
            }
        }

        // TODO: add components and position to create method for convenience
        player = world.entities.create()
        world.entities.addComponent(player, Sprite("circle"))
        world.entities.setPos(player, 3f, 2f)

        createWall(3f, 1f)

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
    }

    private fun createWall(x: Float, y: Float) {
        world.entities.apply {
            val wall = create()
            setPos(wall, x, y)
            addComponent(wall, Sprite("wall2"))
        }
    }
}
