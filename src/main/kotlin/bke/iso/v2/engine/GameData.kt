package bke.iso.v2.engine

import bke.iso.v2.engine.assets.AssetLoader
import kotlin.reflect.KClass

abstract class GameConfig

abstract class GameData(
    val windowTitle: String,
//    val systems: List<System>,
    val assetLoaders: List<AssetLoader<*>>,
    val gameConfig: KClass<out GameConfig>
)
