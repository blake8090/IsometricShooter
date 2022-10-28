package bke.iso.v2.engine

import bke.iso.v2.engine.assets.Assets
import kotlin.reflect.KClass

abstract class GameConfig

abstract class GameData {
    abstract val windowTitle: String
    abstract val gameConfig: KClass<out GameConfig>
    abstract val defaultState: KClass<out State>

    abstract fun addAssetLoaders(assets: Assets)
}
