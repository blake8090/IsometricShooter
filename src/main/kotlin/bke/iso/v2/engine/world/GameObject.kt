package bke.iso.v2.engine.world

import com.badlogic.gdx.math.Vector3
import java.util.UUID

abstract class GameObject {
    abstract val id: UUID
    protected abstract val onMove: (GameObject) -> Unit

    open var x: Float = 0f
        set(value) {
            field = value
            onMove.invoke(this)
        }

    open var y: Float = 0f
        set(value) {
            field = value
            onMove.invoke(this)
        }

    open var z: Float = 0f
        set(value) {
            field = value
            onMove.invoke(this)
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

    override fun equals(other: Any?) =
        other is GameObject && other.id == id

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }

    override fun toString() =
        id.toString()
}
