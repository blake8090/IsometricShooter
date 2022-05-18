package bke.iso

import bke.iso.system.RenderSystem
import bke.iso.system.SystemService
import org.slf4j.LoggerFactory

class Engine {
    private val log = LoggerFactory.getLogger(Engine::class.java)

    private val container = IocContainer()

    init {
        container.registerFromClassPath("bke.iso")
    }

    fun start() {
        log.info("Starting up")
        container.getService<AssetService>().loadAllAssets()
        container.getService<SystemService>().registerSystems(setOf(RenderSystem::class))
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
}
