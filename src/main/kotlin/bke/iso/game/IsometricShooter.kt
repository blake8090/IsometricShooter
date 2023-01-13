package bke.iso.game

import bke.iso.engine.log
import bke.iso.service.Transient
import bke.iso.engine.Game
import bke.iso.engine.asset.AssetService

@Transient
class IsometricShooter(private val assetService: AssetService) : Game {
    override val initialState = GameState::class

    override fun setup() {
        log.debug("setup")
        assetService.addLoader("map", MapLoader::class)
    }
}
