package bke.iso.v2.engine

import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class Game {
    // modules

    private var state: GameState = EmptyState(this)

    fun start() {}

    fun stop() {}

    fun update(deltaTime: Float) {
        // update modules
        state.update(deltaTime)
        for (system in state.systems) {
            system.update(deltaTime)
        }
    }

    fun <T : GameState> switchState(stateClass: KClass<T>) {
        val instance = stateClass.primaryConstructor!!.call(this)
        state.stop()
        state = instance
        instance.start()
    }
}
