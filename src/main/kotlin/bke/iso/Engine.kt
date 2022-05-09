package bke.iso

import bke.iso.ioc.IocContainer
import org.slf4j.LoggerFactory

class Engine {
    private val log = LoggerFactory.getLogger(Engine::class.java)

    private val container = IocContainer()

    fun start() {
        log.info("Starting up")
        container.registerFromClassPath("bke.iso")
    }

    fun update(deltaTime: Float) {
    }

    fun stop() {
        log.info("Shutting down")
    }
}
