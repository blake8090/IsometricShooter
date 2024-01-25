package bke.iso.engine.math

import com.badlogic.gdx.math.Vector3
import kotlin.random.Random

fun Vector3.floor() = apply {
    x = kotlin.math.floor(x)
    y = kotlin.math.floor(y)
    z = kotlin.math.floor(z)
}

fun Vector3.ceil() = apply {
    x = kotlin.math.ceil(x)
    y = kotlin.math.ceil(y)
    z = kotlin.math.ceil(z)
}

/**
 * Subtracts the [other] vector from this vector while avoiding precision errors.
 */
fun Vector3.sub2(other: Vector3): Vector3 {
    this.x = (x.toBigDecimal() - other.x.toBigDecimal()).toFloat()
    this.y = (y.toBigDecimal() - other.y.toBigDecimal()).toFloat()
    this.z = (z.toBigDecimal() - other.z.toBigDecimal()).toFloat()
    return this
}

// TODO: do we really need this?
fun sub(a: Float, b: Float): Float =
    (a.toBigDecimal() - b.toBigDecimal()).toFloat()

// TODO: do we really need this?
fun add(a: Float, b: Float): Float =
    (a.toBigDecimal() + b.toBigDecimal()).toFloat()

fun nextFloat(min: Float, max: Float) =
    Random.nextFloat() * (max - min) + min
