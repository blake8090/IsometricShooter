package bke.iso.game

import bke.iso.engine.Game
import bke.iso.engine.GameConfig
import bke.iso.engine.assets.Assets

class IsometricShooterConfig : GameConfig()

class IsometricShooterGame(private val assets: Assets) : Game {
    override val windowTitle = "Isometric Shooter"
    override val gameConfig = IsometricShooterConfig::class
    override val initialState = GameState::class

    override fun setup() {
        assets.addLoader("map", MapLoader::class)
    }
}
