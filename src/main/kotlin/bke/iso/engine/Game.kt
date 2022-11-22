package bke.iso.engine

import kotlin.reflect.KClass

abstract class GameConfig

interface Game {
    val windowTitle: String
    val gameConfig: KClass<out GameConfig>
    val initialState: KClass<out State>

    fun setup()
}
