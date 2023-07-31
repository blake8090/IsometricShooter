package bke.iso.v2.engine.world

abstract class GameObject {
    abstract val x: Float
    abstract val y: Float
    abstract val z: Float
    protected abstract val onMove: (GameObject) -> Unit
}
