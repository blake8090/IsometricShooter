package bke.iso.editor

import bke.iso.engine.world.Component

data class ActorPrefabReference(val prefab: String) : Component

data class TilePrefabReference(val prefab: String) : Component
