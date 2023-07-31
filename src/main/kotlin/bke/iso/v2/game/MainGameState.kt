package bke.iso.v2.game

import bke.iso.engine.render.Sprite
import bke.iso.v2.engine.Actor
import bke.iso.v2.engine.Game
import bke.iso.v2.engine.GameState
import bke.iso.v2.engine.System

class MainGameState(private val game: Game) : GameState(game) {
    override val systems = emptySet<System>()

    override fun start() {
        game.assets.loadTexture("game\\gfx\\objects\\box.png")

        val box = Actor()
        box.x = 3f
        box.y = 20f
        box.components[Sprite::class] = Sprite("box", 16f, 8f)
        game.world.add(box)
    }
}
