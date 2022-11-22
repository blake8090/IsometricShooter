package bke.iso.engine.event

import bke.iso.engine.log

data class TestEvent(val message: String) : Event()

class TestEventHandler : EventHandler<TestEvent> {
    override fun handle(deltaTime: Float, event: TestEvent) {
        log.info("Test event: '${event.message}'")
    }
}
