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
}
