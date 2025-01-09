package bke.iso.engine.ui

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Skin

abstract class UIComponent(private val skin: Skin) {
    abstract fun create(): Actor
}
