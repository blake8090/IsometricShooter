package bke.iso.editor

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable

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

inline fun <reified T : Any> Skin.get(name: String) =
    get(name, T::class.java)

fun Skin.newTintedDrawable(name: String, colorName: String): Drawable {
    val color = get(colorName, Color::class.java)
    return newDrawable(name, color)
}
