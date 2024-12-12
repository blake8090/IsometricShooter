package bke.iso.engine.core

import bke.iso.engine.Event

interface Module {
    fun start() {}
    fun stop() {}
    fun update(deltaTime: Float) {}
    fun handleEvent(event: Event) {}
}

abstract class EngineModule : Module {
    abstract val moduleName: String
    abstract val updateWhileLoading: Boolean
    abstract val profilingEnabled: Boolean
}
