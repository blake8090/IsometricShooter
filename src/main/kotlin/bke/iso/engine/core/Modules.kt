package bke.iso.engine.core

interface Module {
    val alwaysActive: Boolean
    fun start() {}
    fun stop() {}
    fun update(deltaTime: Float) {}
    fun handleEvent(event: Event) {}
    fun onFrameEnd(deltaTime: Float) {}
}

abstract class EngineModule : Module {
    abstract val moduleName: String
    abstract val updateWhileLoading: Boolean
    abstract val profilingEnabled: Boolean
    override val alwaysActive: Boolean = false
}
