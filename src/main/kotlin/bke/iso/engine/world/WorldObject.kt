package bke.iso.engine.world

import com.badlogic.gdx.math.Vector3

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

    var pos: Vector3
        get() = Vector3(x, y, z)
        set(value) {
            x = value.x
            y = value.y
            z = value.z
        }

    fun move(delta: Vector3) =
        move(delta.x, delta.y, delta.z)

    fun move(dx: Float, dy: Float, dz: Float) {
        x += dx
        y += dy
        z += dz
    }
}
