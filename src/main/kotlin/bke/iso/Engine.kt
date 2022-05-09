package bke.iso

import org.slf4j.LoggerFactory

class Engine {
    private val log = LoggerFactory.getLogger(Engine::class.java)

    fun start() {
        log.info("Starting up")
    }

    fun update(deltaTime: Float) {
    }

    fun stop() {
        log.info("Shutting down")
    }
}
