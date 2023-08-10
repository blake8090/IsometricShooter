package bke.iso.engine

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.TextureLoader
import bke.iso.engine.file.FileSystem
import bke.iso.engine.input.Input
import bke.iso.engine.physics.Collisions
import bke.iso.engine.physics.Physics
import bke.iso.engine.asset.FreeTypeFontGeneratorLoader
import bke.iso.engine.render.Renderer
import bke.iso.engine.ui.UI
import bke.iso.engine.world.World
import bke.iso.game.MainMenuState
import bke.iso.old.engine.log
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

open class Event

abstract class Module {
    protected abstract val game: Game

    open fun start() {}

    open fun update(deltaTime: Float) {}

    open fun stop() {}
}

class Game {
    val assets = Assets(this)
    val fileSystem = FileSystem()
    val input = Input(this)
    val collisions = Collisions(this)
    val physics = Physics(this)
    val renderer = Renderer(this)
    val world = World(this)
    val events = Events()
    val ui = UI(this)

    private var state: GameState = EmptyState(this)

    fun start() {
        assets.addLoader("jpg", TextureLoader())
        assets.addLoader("png", TextureLoader())
        assets.addLoader("ttf", FreeTypeFontGeneratorLoader())
        switchState(MainMenuState::class)
    }

    fun stop() {
        log.info("Stopping game")
    }

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
        ui.update(deltaTime)
    }

    fun resize(width: Int, height: Int) {
        log.info("Resizing to ${width}x$height")
        assets.resize()
        renderer.resize(width, height)
        ui.resize(width, height)
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
            ui.handleEvent(event)
        }
    }
}
