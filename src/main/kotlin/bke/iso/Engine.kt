package bke.iso

import bke.iso.ioc.IocContainer
import org.slf4j.LoggerFactory

class Engine {
    private val log = LoggerFactory.getLogger(Engine::class.java)

    private val container = IocContainer()

    init {
        container.registerFromClassPath("bke.iso")
    }

    fun start() {
        log.info("Starting up")
    }

    fun update(deltaTime: Float) {
    }

    fun stop() {
        log.info("Shutting down")
    }

    fun resolveConfig(): Config =
        container.getService<ConfigService>()
            .resolveConfig()
}
