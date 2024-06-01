package bke.iso.engine

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.cache.ActorPrefabCache
import bke.iso.engine.asset.cache.FontGeneratorCache
import bke.iso.engine.asset.cache.ShaderFileCache
import bke.iso.engine.asset.cache.TextureCache
import bke.iso.engine.asset.cache.TilePrefabCache
import bke.iso.engine.os.Files
import bke.iso.engine.input.Input
import bke.iso.engine.collision.Collisions
import bke.iso.engine.os.Dialogs
import bke.iso.engine.physics.Physics
import bke.iso.engine.profiler.Profiler
import bke.iso.engine.render.Renderer
import bke.iso.engine.scene.SceneCache
import bke.iso.engine.scene.Scenes
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.ui.UI
import bke.iso.engine.ui.loading.EmptyLoadingScreen
import bke.iso.engine.world.World
import bke.iso.game.MainMenuState
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.system.measureTimeMillis

interface Event

class Game {

    private val log = KotlinLogging.logger {}

    val systemInfo = SystemInfo()
    val events: Events = Events()

    val dialogs = Dialogs()
    val files: Files = Files()
    val serializer = Serializer()
    val assets: Assets = Assets(files, systemInfo)

    val world: World = World(events)
    val renderer: Renderer = Renderer(world, assets, events)
    val collisions: Collisions = Collisions(renderer, world)
    val physics: Physics = Physics(world, collisions)
    val scenes = Scenes(assets, serializer, world)

    val input: Input = Input(events)
    val ui: UI = UI(input)

    private var state: State = EmptyState(this)

    private val profiler = Profiler(assets, ui, input)

    fun start() {
        runBlocking {
            init()
        }
    }

    private suspend fun init() {
        val time = measureTimeMillis {
            input.start()
            profiler.start()
            assets.run {
                register(TextureCache())
                register(FontGeneratorCache())
                register(ActorPrefabCache(serializer))
                register(TilePrefabCache(serializer))
                register(SceneCache(serializer))
                register(ShaderFileCache())
            }
            ui.setLoadingScreen(EmptyLoadingScreen(assets))
        }
        log.info { "Initialized modules in $time ms" }

        assets.loadAsync("ui")
        setState(MainMenuState::class)
    }

    fun update(deltaTime: Float) {
        if (!ui.isLoadingScreenActive) {
            profiler.profile("collisions") {
                collisions.update()
            }

            profiler.profile("physics") {
                // TODO: investigate using fixed time step
                physics.update(deltaTime)
            }

            profiler.profile("input") {
                input.update()
            }

            renderer.pointer.update(deltaTime)

            profiler.profile("state") {
                state.update(deltaTime)
            }

            profiler.profile("world") {
                world.update()
            }

            profiler.profile("renderer") {
                renderer.draw()
            }
        }

        profiler.update(deltaTime)

        ui.draw(deltaTime)
        renderer.pointer.draw()
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
        ui.dispose()
        assets.dispose()
        renderer.dispose()
    }

    // TODO: separate into different class!
    inner class Events {
        fun fire(event: Event) {
            state.handleEvent(event)
            ui.handleEvent(event)
        }
    }
}
