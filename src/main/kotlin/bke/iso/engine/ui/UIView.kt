package bke.iso.engine.ui

import bke.iso.engine.core.Event

abstract class UIView {

    abstract fun create()

    abstract fun draw(deltaTime: Float)

    abstract fun handleEvent(event: Event)
}
