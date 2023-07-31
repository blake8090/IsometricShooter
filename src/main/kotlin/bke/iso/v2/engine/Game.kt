package bke.iso.v2.engine

import bke.iso.v2.game.MainGameState
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

abstract class Module(game: Game) {
    open fun start() {}
    open fun update(deltaTime: Float) {}
    open fun stop() {}
}

class Game {
    // modules
    val assets = Assets(this)
    val renderer = Renderer(this)
    val world = World(this)

    private var state: GameState = EmptyState(this)

    fun start() {
        switchState(MainGameState::class)
    }

    fun stop() {}

    fun update(deltaTime: Float) {
        // update modules

        state.update(deltaTime)
        for (system in state.systems) {
            system.update(deltaTime)
        }

        renderer.render()
    }

    fun <T : GameState> switchState(stateClass: KClass<T>) {
        val instance = stateClass.primaryConstructor!!.call(this)
        state.stop()
        state = instance
        instance.start()
    }
}
