package bke.iso.old.game

import bke.iso.old.engine.Game
import bke.iso.old.engine.asset.AssetService
import bke.iso.old.game.asset.GameMapLoader

class IsometricShooter(private val assetService: AssetService) : Game {
    override val initialState = GameState::class

    override fun setup() {
        assetService.addLoader<GameMapLoader>("map2")
        // TODO: maybe load very first module here, for example "menu"
    }
}
