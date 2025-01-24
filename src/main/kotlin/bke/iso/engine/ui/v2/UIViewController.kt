package bke.iso.engine.ui.v2

import bke.iso.engine.core.Event
import com.badlogic.gdx.scenes.scene2d.Event as GdxEvent

abstract class UIViewController<T : UIView>(val view: T) {

    open fun start() {}

    open fun stop() {}

    open fun update(deltaTime: Float) {}

    open fun handleEvent(event: Event) {}

    open fun handleEvent(event: GdxEvent) {}
}
