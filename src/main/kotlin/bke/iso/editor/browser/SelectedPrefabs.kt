package bke.iso.editor.browser

import bke.iso.engine.asset.cache.ActorPrefab
import bke.iso.engine.asset.cache.TilePrefab

sealed class SelectedPrefab {
    abstract val texture: String
}

data class SelectedActorPrefab(
    val prefab: ActorPrefab,
    override val texture: String
) : SelectedPrefab()

data class SelectedTilePrefab(
    val prefab: TilePrefab,
    override val texture: String
) : SelectedPrefab()
