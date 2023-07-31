package bke.iso.v2.game

import bke.iso.engine.math.Location
import bke.iso.engine.render.Sprite
import bke.iso.v2.engine.Game
import bke.iso.v2.engine.GameState
import bke.iso.v2.engine.System

class MainGameState(private val game: Game) : GameState(game) {
    override val systems = emptySet<System>()

    override fun start() {
        game.assets.loadTexture("game\\gfx\\objects\\box.png")
        game.assets.loadTexture("game\\gfx\\tiles\\floor.png")

        game.world.newActor(3f, 20f, 0f, Sprite("box", 16f, 8f))
        game.world.setTile(Location(3, 20, 0), "floor")
        game.world.setTile(Location(3, 19, 0), "floor")
    }
}
