package bke.iso.v2.engine

import bke.iso.v2.engine.assets.AssetLoader
import kotlin.reflect.KClass

abstract class GameConfig

abstract class GameData(
    val windowTitle: String,
    val gameConfig: KClass<out GameConfig>,
    val defaultState: KClass<out State>,
    val assetLoaders: List<AssetLoader<*>>
)
