package bke.iso.engine.core

interface Module {
    fun start() {}
    fun stop() {}
    fun update(deltaTime: Float) {}
    fun handleEvent(event: Event) {}
    fun onFrameEnd() {}
}

abstract class EngineModule : Module {
    abstract val moduleName: String
    abstract val updateWhileLoading: Boolean
    abstract val profilingEnabled: Boolean
}
