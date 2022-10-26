package bke.iso.v2.game

import bke.iso.v2.engine.GameConfig
import bke.iso.v2.engine.GameData

class IsometricShooterConfig : GameConfig()

class IsometricShooter : GameData(
    windowTitle = "Isometric Shooter",
    gameConfig = IsometricShooterConfig::class,
    defaultState = GameState::class,
    assetLoaders = emptyList()
)
