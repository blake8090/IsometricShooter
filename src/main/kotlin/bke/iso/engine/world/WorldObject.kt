package bke.iso.engine.world

abstract class WorldObject {

    val positionListeners: MutableList<(WorldObject) -> Unit> = mutableListOf()

    abstract val layer: Int

    var x: Float = 0f
        set(value) {
            field = value
            positionListeners.forEach { listener -> listener.invoke(this) }
        }

    var y: Float = 0f
        set(value) {
            field = value
            positionListeners.forEach { listener -> listener.invoke(this) }
        }

    var z: Float = 0f
        set(value) {
            field = value
            positionListeners.forEach { listener -> listener.invoke(this) }
        }
}
