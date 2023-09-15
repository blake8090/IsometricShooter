package bke.iso.editor.browser

import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab

sealed class PrefabData {
    abstract val texture: String
}

data class ActorPrefabData(
    val prefab: ActorPrefab,
    override val texture: String
) : PrefabData()

data class TilePrefabData(
    val prefab: TilePrefab,
    override val texture: String
) : PrefabData()
