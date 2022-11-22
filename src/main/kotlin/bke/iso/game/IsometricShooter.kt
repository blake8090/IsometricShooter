package bke.iso.game

import bke.iso.engine.Game
import bke.iso.engine.GameConfig
import bke.iso.engine.State
import bke.iso.engine.assets.Assets
import kotlin.reflect.KClass

class IsometricShooterConfig : GameConfig()

class IsometricShooterGame(private val assets: Assets) : Game {
    override val windowTitle: String
        get() = "Isometric Shooter"
    override val gameConfig: KClass<out GameConfig>
        get() = IsometricShooterConfig::class
    override val initialState: KClass<out State>
        get() = GameState::class

    override fun setup() {
        assets.addLoader("map", MapLoader::class)
    }
}
