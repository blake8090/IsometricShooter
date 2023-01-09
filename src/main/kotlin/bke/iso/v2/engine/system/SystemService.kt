package bke.iso.v2.engine.system

import bke.iso.service.Singleton
import bke.iso.v2.engine.state.State

@Singleton
class SystemService {

    private val baseSystems = mutableListOf<System>()

    fun update(state: State, deltaTime: Float) {
        baseSystems.forEach { system -> system.update(deltaTime) }
        state.systems.forEach { system -> system.update(deltaTime) }
    }
}
