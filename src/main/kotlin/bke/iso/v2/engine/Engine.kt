package bke.iso.v2.engine

import bke.iso.engine.util.getLogger
import bke.iso.v2.app.service.Service

@Service
class Engine {
    private val log = getLogger()

    fun start() {
        log.info("Starting up")
        // TODO: add asset loaders here
    }

    fun update(deltaTime: Float) {

    }

    fun stop() {
        log.info("Shutting down")
    }
}
