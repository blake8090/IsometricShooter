package bke.iso

import bke.iso.asset.AssetService
import bke.iso.system.RenderSystem
import bke.iso.system.SystemService
import bke.iso.util.FilePointer
import bke.iso.util.Globals
import kotlinx.coroutines.launch
import ktx.async.KtxAsync
import org.slf4j.LoggerFactory
import java.io.File

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
        val assetLoader = container.getService<AssetService>()
        val globals = container.getService<Globals>()
        val loadingStartTime = System.currentTimeMillis()
        KtxAsync.launch {
            File(globals.assetsDirectory)
                .walkTopDown()
                .map(::FilePointer)
                .forEach(assetLoader::loadAsset)

            val loadingEndTime = System.currentTimeMillis()
            log.info("Loaded assets in ${loadingEndTime - loadingStartTime} millis")
        }
    }
}

