package bke.iso.engine.math

import com.badlogic.gdx.math.Vector3
import kotlinx.serialization.Serializable
import kotlin.math.floor

@Serializable
data class Location(
    val x: Int = 0,
    val y: Int = 0,
    val z: Int = 0
) {
    constructor(x: Float, y: Float, z: Float) :
            this(
                floor(x).toInt(),
                floor(y).toInt(),
                floor(z).toInt()
            )

    constructor(pos: Vector3) : this(pos.x, pos.y, pos.z)

    fun toVector3(): Vector3 =
        Vector3(x.toFloat(), y.toFloat(), z.toFloat())
}
