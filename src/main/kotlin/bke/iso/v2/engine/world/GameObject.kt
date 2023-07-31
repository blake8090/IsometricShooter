package bke.iso.v2.engine.world

import java.util.UUID

abstract class GameObject {
    abstract val id: UUID
    abstract val x: Float
    abstract val y: Float
    abstract val z: Float
    protected abstract val onMove: (GameObject) -> Unit
}
