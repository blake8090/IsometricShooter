package bke.iso.engine.system

import bke.iso.engine.physics.PhysicsSystem
import bke.iso.engine.state.State
import bke.iso.service.v2.ServiceProvider
import bke.iso.service.v2.SingletonService

class SystemService(private val provider: ServiceProvider<System>) : SingletonService {

    private val baseSystems = mutableListOf<System>()

    override fun create() {
        baseSystems.add(provider.get(PhysicsSystem::class))
    }

    fun update(state: State, deltaTime: Float) {
        baseSystems.forEach { system -> system.update(deltaTime) }
        state.systems.forEach { system -> system.update(deltaTime) }
    }
}
