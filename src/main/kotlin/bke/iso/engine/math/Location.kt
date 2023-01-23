package bke.iso.engine.math

import com.badlogic.gdx.math.Vector3
import kotlin.math.floor

data class Location(val x: Int, val y: Int, val z: Int) {
    constructor(x: Float, y: Float, z: Float = 0f) :
            this(
                floor(x).toInt(),
                floor(y).toInt(),
                floor(z).toInt()
            )

    fun toVector3() =
        Vector3(x.toFloat(), y.toFloat(), z.toFloat())
}
