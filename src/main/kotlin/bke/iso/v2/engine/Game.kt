package bke.iso.v2.engine

import bke.iso.engine.asset.TextureLoader
import bke.iso.engine.event.Event
import bke.iso.v2.engine.asset.Assets
import bke.iso.v2.engine.input.Input
import bke.iso.v2.engine.physics.Collisions
import bke.iso.v2.engine.physics.Physics
import bke.iso.v2.engine.render.Renderer
import bke.iso.v2.engine.world.World
import bke.iso.v2.game.GameplayState
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

abstract class Module(game: Game) {
    open fun start() {}
    open fun update(deltaTime: Float) {}
    open fun stop() {}
}

class Game {
    val assets = Assets(this)
    val input = Input(this)
    val collisions = Collisions(this)
    val physics = Physics(this)
    val renderer = Renderer(this)
    val world = World(this)
    val events = Events()

    private var state: GameState = EmptyState(this)

    fun start() {
        assets.addLoader("jpg", TextureLoader())
        assets.addLoader("png", TextureLoader())
        switchState(GameplayState::class)
    }

    fun stop() {}

    fun update(deltaTime: Float) {
        input.update(deltaTime)
        physics.update(deltaTime)

        state.update(deltaTime)
        for (system in state.systems) {
            system.update(deltaTime)
        }

        renderer.render()
        collisions.update(deltaTime)
        world.update(deltaTime)
    }

    fun <T : GameState> switchState(stateClass: KClass<T>) {
        val instance = stateClass.primaryConstructor!!.call(this)
        state.stop()
        state = instance
        instance.start()
    }

    inner class Events {
        fun fire(event: Event) {
            state.handleEvent(event)
        }
    }
}
