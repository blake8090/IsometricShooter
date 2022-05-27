package bke.iso

import bke.iso.asset.AssetService
import bke.iso.map.MapService
import bke.iso.system.RenderSystem
import bke.iso.system.SystemService
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import org.slf4j.LoggerFactory

class Engine {
    private val log = LoggerFactory.getLogger(Engine::class.java)

    private val container = IocContainer()

    init {
        container.registerFromClassPath("bke.iso")
    }

    fun start() {
        log.info("Starting up")
        container.getService<SystemService>().registerSystems(mutableSetOf(RenderSystem::class))
        loadAssets()

        container.getService<MapService>().loadMap("test")
    }

    fun update(deltaTime: Float) {
        container.getService<SystemService>().update(deltaTime)
    }

    fun stop() {
        log.info("Shutting down")
    }

    fun resolveConfig(): Config =
        container.getService<ConfigService>()
            .resolveConfig()

    private fun loadAssets() {
        container.getService<AssetService>().apply {
            // TODO: use globals
            setupAssetLoaders("bke.iso")
            KtxAsync.launch {
                loadAssets("assets")
            }
        }
    }
}

