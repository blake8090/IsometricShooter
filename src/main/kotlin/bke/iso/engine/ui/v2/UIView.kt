package bke.iso.engine.ui.v2

import com.badlogic.gdx.scenes.scene2d.ui.Table

abstract class UIView {
    val root = Table()

    abstract fun create()
}
