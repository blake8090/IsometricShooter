package bke.iso.game

import bke.iso.engine.GameConfig
import bke.iso.engine.GameData
import bke.iso.engine.assets.Assets

class IsometricShooterConfig : GameConfig()

class IsometricShooter : GameData() {
    override val windowTitle = "Isometric Shooter"
    override val gameConfig = IsometricShooterConfig::class
    override val defaultState = GameState::class

    override fun addAssetLoaders(assets: Assets) {
        assets.addLoader("map", MapLoader::class)
    }
}
