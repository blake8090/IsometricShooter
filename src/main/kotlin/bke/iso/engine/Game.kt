package bke.iso.engine

import bke.iso.engine.asset.Assets
import bke.iso.engine.file.FileSystem
import bke.iso.engine.input.Input
import bke.iso.engine.physics.Collisions
import bke.iso.engine.physics.Physics
import bke.iso.engine.render.Renderer
import bke.iso.engine.ui.UI
import bke.iso.engine.world.World
import bke.iso.game.MainMenuState
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import ktx.async.KtxAsync
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.time.measureTime

abstract class Event

class Game {

    private val log = KotlinLogging.logger {}

    val events = Events()
    val assets = Assets(this)
    val fileSystem = FileSystem()
    val input = Input(this)
    val collisions = Collisions(this)
    val physics = Physics(this)
    val renderer = Renderer(this)
    val world = World(this)
    val ui = UI(this)

    private var state: GameState = EmptyState(this)
    private var loading = false

    fun start() {
        input.start()
        assets.setup()
        switchState(MainMenuState::class)
    }

    fun stop() {
        log.info { "Stopping game" }
    }

    fun update(deltaTime: Float) {
        if (!loading) {
            runFrame(deltaTime)
        }
        ui.update(deltaTime)
    }

    private fun runFrame(deltaTime: Float) {
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

    fun resize(width: Int, height: Int) {
        log.info("Resizing to ${width}x$height")
        renderer.resize(width, height)
        ui.resize(width, height)
    }

    fun <T : GameState> switchState(stateClass: KClass<T>) {
        log.debug { "switching to state ${stateClass.simpleName}" }
        state = stateClass.primaryConstructor!!.call(this)
        load(state)
    }

    private fun load(state: GameState) {
        loading = true
        state.loadingScreen?.let(ui::setScreen)
        KtxAsync.async {
            if (state.loadingScreen != null) {
                // delay a bit to give time for the loading screen to show up
                delay(300)
            }
            val duration = measureTime {
                log.debug { "Loading started" }
                state.load()
                state.start()
                loading = false
            }
            log.debug { "Loading finished in ${duration.inWholeMilliseconds} ms" }
        }
    }

    inner class Events {
        fun fire(event: Event) {
            state.handleEvent(event)
            ui.handleEvent(event)
        }
    }
}

abstract class Module {
    protected abstract val game: Game

    open fun update(deltaTime: Float) {}

    open fun stop() {}
}
