package bke.iso.v2.game

import bke.iso.v2.engine.State
import bke.iso.v2.engine.log

class GameState : State() {
    override fun start() {
        log.info("starting up")
    }
}