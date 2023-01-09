package bke.iso.v2.engine.math

import com.badlogic.gdx.math.Vector2
import kotlin.math.floor

data class Location(val x: Int, val y: Int) {
    constructor(x: Float, y: Float) :
            this(
                floor(x).toInt(),
                floor(y).toInt()
            )

    fun toVector2() =
        Vector2(x.toFloat(), y.toFloat())
}
