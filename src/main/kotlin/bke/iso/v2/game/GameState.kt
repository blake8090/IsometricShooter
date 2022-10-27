package bke.iso.v2.game

import bke.iso.v2.engine.State
import bke.iso.v2.engine.log
import bke.iso.v2.engine.world.Sprite
import bke.iso.v2.engine.world.Tile
import bke.iso.v2.engine.world.World

class GameState(private val world: World) : State() {
    override fun start() {
        log.debug("building world")

        val tile = Tile("floor")
        for (y in 0..5) {
            for (x in 0..10) {
                world.setTile(tile, x, y)
            }
        }

        val testEntity = world.entities.create()
        world.entities.addComponent(testEntity, Sprite("circle"))

        log.debug("finished!")
    }
}
