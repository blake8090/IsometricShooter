package bke.iso.engine

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.prefab.ActorPrefabCache
import bke.iso.engine.asset.font.FontGeneratorCache
import bke.iso.engine.asset.shader.ShaderFileCache
import bke.iso.engine.asset.TextureCache
import bke.iso.engine.asset.config.ConfigCache
import bke.iso.engine.asset.prefab.TilePrefabCache
import bke.iso.engine.asset.shader.ShaderInfoCache
import bke.iso.engine.os.Files
import bke.iso.engine.input.Input
import bke.iso.engine.collision.Collisions
import bke.iso.engine.os.Dialogs
import bke.iso.engine.os.SystemInfo
import bke.iso.engine.physics.Physics
import bke.iso.engine.profiler.Profiler
import bke.iso.engine.render.Renderer
import bke.iso.engine.scene.SceneCache
import bke.iso.engine.scene.Scenes
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.state.States
import bke.iso.engine.ui.UI
import bke.iso.engine.ui.loading.EmptyLoadingScreen
import bke.iso.engine.world.World
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking

class Game {

    private val log = KotlinLogging.logger {}

    val systemInfo = SystemInfo()
    val events: Events = Events(::handleEvent)

    val dialogs = Dialogs()
    val files: Files = Files()
    val serializer = Serializer()
    val assets: Assets = Assets(files, systemInfo)

    val world: World = World(events)
    val renderer: Renderer = Renderer(world, assets, events)
    val collisions: Collisions = Collisions(renderer, world)
    val physics: Physics = Physics(world, collisions)
    val scenes = Scenes(assets, serializer, world, renderer)

    val input: Input = Input(events)
    val ui: UI = UI(input)

    val states = States(this)

    private val profiler = Profiler(assets, ui, input)

    fun start(gameInfo: GameInfo) {
        input.start()
        profiler.start()

        assets.addCache(TextureCache())
        assets.addCache(FontGeneratorCache())
        assets.addCache(ActorPrefabCache(serializer))
        assets.addCache(TilePrefabCache(serializer))
        assets.addCache(SceneCache(serializer))
        assets.addCache(ShaderFileCache())
        assets.addCache(ShaderInfoCache(serializer))
        assets.addCache(ConfigCache(serializer))

        ui.setLoadingScreen(EmptyLoadingScreen(assets))

        runBlocking {
            assets.loadAsync("ui")
        }

        gameInfo.start(this)
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
                states.currentState.update(deltaTime)
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

    private fun handleEvent(event: Event) {
        states.currentState.handleEvent(event)
        ui.handleEvent(event)
    }
}
