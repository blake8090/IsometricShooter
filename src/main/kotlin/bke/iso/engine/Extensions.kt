package bke.iso.engine

import com.badlogic.gdx.math.Vector3

inline fun <reified T : Any> Collection<*>.withFirstInstance(action: (T) -> Unit) =
    filterIsInstance<T>()
        .firstOrNull()
        ?.let { instance -> action.invoke(instance) }

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
