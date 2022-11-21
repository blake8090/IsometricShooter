package bke.iso.engine

import bke.iso.engine.system.System
import kotlin.reflect.KClass

abstract class State {
    open fun start() {}

    open fun update(deltaTime: Float) {}

    open fun stop() {}

    open fun getSystems(): List<KClass<out System>> =
        emptyList()
}

class EmptyState : State()
