package bke.iso.engine

import bke.iso.engine.asset.Assets
import bke.iso.engine.asset.entity.EntityTemplateAssetCache
import bke.iso.engine.asset.font.FontGeneratorAssetCache
import bke.iso.engine.asset.shader.ShaderFileAssetCache
import bke.iso.engine.asset.texture.TextureAssetCache
import bke.iso.engine.asset.config.ConfigAssetCache
import bke.iso.engine.asset.shader.ShaderInfoAssetCache
import bke.iso.engine.os.Files
import bke.iso.engine.input.Input
import bke.iso.engine.collision.Collisions
import bke.iso.engine.core.EngineModule
import bke.iso.engine.core.Event
import bke.iso.engine.core.Events
import bke.iso.engine.core.Game
import bke.iso.engine.lighting.Lighting
import bke.iso.engine.loading.LoadActionCompleteEvent
import bke.iso.engine.loading.LoadingScreens
import bke.iso.engine.os.Dialogs
import bke.iso.engine.os.SystemInfo
import bke.iso.engine.physics.Physics
import bke.iso.engine.profiler.Profiler
import bke.iso.engine.render.Renderer
import bke.iso.engine.render.RendererManager
import bke.iso.engine.scene.SceneAssetCache
import bke.iso.engine.scene.Scenes
import bke.iso.engine.serialization.Serializer
import bke.iso.engine.state.States
import bke.iso.engine.ui.UI
import bke.iso.engine.world.World
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.profiling.GLProfiler
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking

class Engine(val game: Game) {

    private val log = KotlinLogging.logger {}

    private val files: Files = Files()
    private val systemInfo = SystemInfo(game)
    val dialogs = Dialogs()

    val states = States(this)
    val events: Events = Events(::handleEvent)
    val input: Input = Input(events)
    val ui: UI = UI(input)

    val serializer = Serializer()
    val assets: Assets = Assets(files, systemInfo)

    val world: World = World(events)

    val lighting = Lighting(world)
    val renderer: Renderer = Renderer(world, assets, lighting, events)
    val rendererManager = RendererManager(renderer)

    val collisions: Collisions = Collisions(renderer, world)
    val physics: Physics = Physics(world, collisions)
    val scenes = Scenes(assets, serializer, world, renderer, lighting)

    val loadingScreens = LoadingScreens(events)


    private val modules = listOf(
        collisions,
        physics,
        input,
        states,
        world,
        rendererManager,
        ui,
        loadingScreens,
        lighting
    )
    private val glProfiler = GLProfiler(Gdx.graphics)
    private val profiler = Profiler(ui, input, glProfiler)

    var gamePaused = false
        private set

    fun start() {
        glProfiler.enable()
        systemInfo.logInfo()

        for (module in modules) {
            module.start()
        }

        profiler.start()

        assets.addCache(TextureAssetCache())
        assets.addCache(FontGeneratorAssetCache())
        assets.addCache(EntityTemplateAssetCache(serializer))
        assets.addCache(SceneAssetCache(serializer))
        assets.addCache(ShaderFileAssetCache())
        assets.addCache(ShaderInfoAssetCache(serializer))
        assets.addCache(ConfigAssetCache(serializer))

        runBlocking {
            assets.loadAsync("ui")
        }

        game.start(this)
    }

    fun update(deltaTime: Float) {
        glProfiler.reset()
        updateModule(collisions, deltaTime)
        updateModule(physics, deltaTime)
        updateModule(input, deltaTime)

        if (!loadingScreens.isLoading()) {
            renderer.pointer.update(deltaTime)
        }

        updateModule(states, deltaTime)
        updateModule(world, deltaTime)
        updateModule(lighting, deltaTime)
        updateModule(rendererManager, deltaTime)
        updateModule(ui, deltaTime)
        profiler.update(deltaTime)

        if (!loadingScreens.isLoading()) {
            renderer.pointer.draw()
        }

        updateModule(loadingScreens, deltaTime)

        for (module in modules) {
            module.onFrameEnd(deltaTime)
        }
    }

    private fun updateModule(
        module: EngineModule,
        deltaTime: Float,
        overrideLoadingScreen: Boolean = false
    ) {
        if (!canUpdate(module, overrideLoadingScreen)) {
            return
        }

        if (module.profilingEnabled) {
            profiler.profile(module.moduleName) {
                module.update(deltaTime)
            }
        } else {
            module.update(deltaTime)
        }
    }

    private fun canUpdate(module: EngineModule, overrideLoadingScreen: Boolean = false): Boolean =
        if (gamePaused && !module.alwaysActive) {
            false
        } else if (loadingScreens.isLoading() && !module.updateWhileLoading && !overrideLoadingScreen) {
            false
        } else {
            true
        }

    fun resize(width: Int, height: Int) {
        log.info { "Resizing to ${width}x$height" }
        rendererManager.resize(width, height)
        ui.resize(width, height)
    }

    fun stop() {
        log.info { "Stopping game" }

        for (module in modules) {
            module.stop()
        }

        assets.dispose()
    }

    private fun handleEvent(event: Event) {
        for (module in modules) {
            module.handleEvent(event)
        }

        when (event) {
            is GamePaused -> {
                gamePaused = true
            }

            is GameResumed -> {
                gamePaused = false
            }

            // we run the states module for one frame to make sure the camera is updated to the player's position.
            // this fixes an issue where the camera is in the wrong location when the loading screen is transitioning out.
            is LoadActionCompleteEvent -> {
                log.debug { "--- Running state for 1 frame --- " }
                updateModule(states, Gdx.graphics.deltaTime, true)
                log.debug { "--- End state --- " }
            }
        }
    }

    class GamePaused : Event
    class GameResumed : Event
}
