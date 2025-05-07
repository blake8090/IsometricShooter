package bke.iso.editor2.scene

import bke.iso.engine.world.actor.Component

data class ActorPrefabReference(val prefab: String = "") : Component

data class TilePrefabReference(val prefab: String = "") : Component
