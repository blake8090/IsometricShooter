package bke.iso.engine

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.FreeTypeFontGeneratorLoader
import bke.iso.engine.asset.loader.TextureLoader
import bke.iso.engine.asset.prefab.ActorPrefabLoader
import bke.iso.engine.file.Files
import bke.iso.engine.input.Input
import bke.iso.engine.collision.Collisions
import bke.iso.engine.physics.Physics
import bke.iso.engine.render.Renderer
import bke.iso.engine.ui.UI
import bke.iso.engine.ui.loading.BasicLoadingScreen
import bke.iso.engine.world.World
import bke.iso.game.MainMenuState
import com.badlogic.gdx.utils.PerformanceCounter
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

interface Event

class Game {

    private val log = KotlinLogging.logger {}

    val systemInfo = SystemInfo()
    val events: Events = Events()

    val files: Files = Files()
    val serializer = Serializer()
    val assets: Assets = Assets(files, systemInfo)

    val world: World = World()
    val renderer: Renderer = Renderer(world, assets, events)
    val collisions: Collisions = Collisions(renderer, world)
    val physics: Physics = Physics(world, collisions)

    val input: Input = Input(events)
    val ui: UI = UI(input)

    private var state: State = EmptyState(this)

    private val performanceCounter = PerformanceCounter("renderer")

    fun start() {
        serializer.start()
        input.start()

        assets.run {
            register(TextureLoader())
            register(FreeTypeFontGeneratorLoader())
            register(ActorPrefabLoader(serializer))

            runBlocking {
                loadAsync("ui")
                ui.setLoadingScreen(BasicLoadingScreen(assets))
                setState(MainMenuState::class)
            }
        }
    }

    fun update(deltaTime: Float) {
        runFrame(deltaTime)
        ui.draw(deltaTime)
        renderer.drawCursor()
    }

    private fun runFrame(deltaTime: Float) {
        if (ui.isLoadingScreenActive) {
            return
        }

        collisions.update()
        // TODO: investigate using fixed time step
        physics.update(deltaTime)

        input.update()
        renderer.updateCursor(deltaTime)
        state.update(deltaTime)
        world.update()

//        performanceCounter.start()
        renderer.draw()
//        performanceCounter.stop()
//        performanceCounter.tick()
//        val mean = performanceCounter.time.value * 1000f
//        val max = performanceCounter.time.max * 1000f
//        log.info { "renderer.draw() - max: ${max}ms mean: ${mean}ms load: ${performanceCounter.load.value}" }
    }

    fun <T : State> setState(type: KClass<T>) {
        log.debug { "Switching to state ${type.simpleName}" }
        state = requireNotNull(type.primaryConstructor).call(this)
        ui.loadingScreen.start(state::load)
    }

    fun resize(width: Int, height: Int) {
        log.info { "Resizing to ${width}x$height" }
        renderer.resize(width, height)
        ui.resize(width, height)
    }

    fun stop() {
        log.info { "Stopping game" }
        assets.dispose()
        renderer.dispose()
        ui.dispose()
    }

    // TODO: separate into different class!
    inner class Events {
        fun fire(event: Event) {
            state.handleEvent(event)
            ui.handleEvent(event)
        }
    }
}
