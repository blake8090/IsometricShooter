package bke.iso.editor.scene

import bke.iso.engine.world.entity.Component

data class ActorPrefabReference(val prefab: String = "") : Component

data class TilePrefabReference(val prefab: String = "") : Component
