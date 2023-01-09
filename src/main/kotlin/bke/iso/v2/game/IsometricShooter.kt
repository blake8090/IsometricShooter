package bke.iso.v2.game

import bke.iso.engine.log
import bke.iso.service.Transient
import bke.iso.v2.engine.Game

@Transient
class IsometricShooter : Game {
    override val initialState = GameState::class

    override fun setup() {
        log.debug("setup")
    }
}
