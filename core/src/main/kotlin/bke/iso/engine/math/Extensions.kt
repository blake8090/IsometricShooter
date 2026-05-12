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

fun nextFloat(min: Float, max: Float) =
    Random.nextFloat() * (max - min) + min
