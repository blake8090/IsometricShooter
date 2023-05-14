package bke.iso.game

import bke.iso.engine.Game
import bke.iso.engine.asset.AssetService
import bke.iso.game.map.GameMapLoader

class IsometricShooter(private val assetService: AssetService) : Game {
    override val initialState = GameState::class

    override fun setup() {
        assetService.addLoader<MapDataLoader>("map")
        assetService.addLoader<GameMapLoader>("map2")
        // TODO: maybe load very first module here, for example "menu"
    }
}
