package bke.iso.editor

import com.badlogic.gdx.graphics.Color

inline fun <reified T : Any> Collection<*>.withFirstInstance(action: (T) -> Unit) =
    filterIsInstance<T>()
        .firstOrNull()
        ?.let { instance -> action.invoke(instance) }

/**
 * Utility to create a [Color] from RGBA values provided in a 0 to 255 range
 */
fun color(r: Int, g: Int, b: Int, a: Int = 255): Color =
    Color(
        (r / 255f).coerceIn(0f, 1f),
        (g / 255f).coerceIn(0f, 1f),
        (b / 255f).coerceIn(0f, 1f),
        (a / 255f).coerceIn(0f, 1f)
    )
