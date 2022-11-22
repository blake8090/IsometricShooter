package bke.iso.game

import bke.iso.engine.Game
import bke.iso.engine.GameConfig
import bke.iso.engine.State
import kotlin.reflect.KClass

class IsometricShooterConfig : GameConfig()

class IsometricShooterGame : Game {
    override val windowTitle: String
        get() = "Isometric Shooter"
    override val gameConfig: KClass<out GameConfig>
        get() = IsometricShooterConfig::class
    override val initialState: KClass<out State>
        get() = GameState::class

    override fun setup() {
    }
}
