package bke.iso.game

import bke.iso.engine.GameInfo
import bke.iso.engine.di.SingletonImpl

@SingletonImpl(GameInfo::class)
class IsometricShooter : GameInfo(
    windowTitle = "Isometric Shooter",
    defaultState = GameState::class
)
