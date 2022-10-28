package bke.iso.v2.game

import bke.iso.v2.engine.GameConfig
import bke.iso.v2.engine.GameData
import bke.iso.v2.engine.assets.Assets

class IsometricShooterConfig : GameConfig()

class IsometricShooter : GameData() {
    override val windowTitle = "Isometric Shooter"
    override val gameConfig = IsometricShooterConfig::class
    override val defaultState = GameState::class

    override fun addAssetLoaders(assets: Assets) {
        assets.addLoader("map", MapLoader::class)
    }
}
