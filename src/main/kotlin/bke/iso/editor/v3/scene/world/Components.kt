package bke.iso.editor.v3.scene.world

import bke.iso.engine.world.actor.Component

data class ActorPrefabReference(val name: String = "") : Component

data class TilePrefabReference(val name: String = "") : Component
