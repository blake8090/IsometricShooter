package bke.iso.game

import bke.iso.EmptyState
import bke.iso.GameInfo
import bke.iso.di.SingletonImpl

@SingletonImpl(GameInfo::class)
class IsometricShooter : GameInfo(
    windowTitle = "Isometric Shooter",
    defaultState = EmptyState::class
)
