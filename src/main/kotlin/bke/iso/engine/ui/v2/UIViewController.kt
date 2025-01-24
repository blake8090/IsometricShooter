package bke.iso.engine.ui.v2

import bke.iso.engine.core.Event
import com.badlogic.gdx.scenes.scene2d.Event as GdxEvent

abstract class UIViewController<T : UIView>(val view: T) {

    var enabled = true
        set(value) {
            field = value
            if (value) {
                enabled()
            } else {
                disabled()
            }
        }

    open fun start() {}

    open fun stop() {}

    open fun update(deltaTime: Float) {}

    open fun handleEvent(event: Event) {}

    open fun handleEvent(event: GdxEvent) {}

    protected open fun enabled() {}

    protected open fun disabled() {}
}
