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

    private var state: State = EmptyState(this)
    private var loading = false

    fun start() {
        assets.start()
        input.start()
        switchState(MainMenuState::class)
    }

    fun stop() {
        log.info { "Stopping game" }
        assets.dispose()
        renderer.dispose()
        ui.dispose()
    }

    fun update(deltaTime: Float) {
        if (!loading) {
            runFrame(deltaTime)
        }
        ui.update(deltaTime)
        renderer.drawCursor()
    }

    private fun runFrame(deltaTime: Float) {
        input.update(deltaTime)
        renderer.updateCursor(deltaTime)
        state.update(deltaTime)
        world.update(deltaTime)
        collisions.update(deltaTime)
        physics.update(deltaTime)
        renderer.render()
    }

    fun resize(width: Int, height: Int) {
        log.info("Resizing to ${width}x$height")
        renderer.resize(width, height)
        ui.resize(width, height)
    }

    fun <T : State> switchState(type: KClass<T>) {
        log.debug { "switching to state ${type.simpleName}" }

        loading = true
        state = type.primaryConstructor!!.call(this)
        state.loadingScreen?.let(ui::setScreen)
        KtxAsync.async { load(state) }
    }

    private suspend fun load(state: State) {
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

    inner class Events {
        fun fire(event: Event) {
            state.handleEvent(event)
            ui.handleEvent(event)
        }
    }
}

abstract class Module {
    protected abstract val game: Game

    open fun start() {}

    open fun update(deltaTime: Float) {}

    open fun dispose() {}
}
