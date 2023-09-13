package bke.iso.engine.ui.util

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.Drawable

inline fun <reified T : Any> Skin.get(name: String) =
    get(name, T::class.java)

fun Skin.newTintedDrawable(name: String, colorName: String): Drawable =
    newDrawable(name, getColor(colorName))
