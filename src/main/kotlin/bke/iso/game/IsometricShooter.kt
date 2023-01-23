package bke.iso.game

import bke.iso.service.Transient
import bke.iso.engine.Game
import bke.iso.engine.asset.AssetService

@Transient
class IsometricShooter(private val assetService: AssetService) : Game {
    override val initialState = GameState::class

    override fun setup() {
        assetService.addLoader<MapDataLoader>("map")
        // TODO: maybe load very first module here, for example "menu"
    }
}
