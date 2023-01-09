package bke.iso.v2.game

import bke.iso.engine.log
import bke.iso.service.Transient
import bke.iso.v2.engine.asset.AssetService
import bke.iso.v2.engine.state.State
import bke.iso.v2.engine.system.System

@Transient
class GameState(private val assetService: AssetService) : State() {
    override val systems = emptySet<System>()

    override fun start() {
        log.debug("on start")
        assetService.load("assets")
    }
}
