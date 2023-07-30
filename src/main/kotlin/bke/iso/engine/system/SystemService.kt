package bke.iso.engine.system

import bke.iso.engine.physics.PhysicsSystem
import bke.iso.engine.physics.CollisionSystem
import bke.iso.engine.state.StateService
import bke.iso.service.ServiceProvider
import bke.iso.service.SingletonService

class SystemService(
    private val stateService: StateService,
    private val provider: ServiceProvider<System>
) : SingletonService {

    private val baseSystems = mutableListOf<System>()

    override fun create() {
        baseSystems.add(provider.get(PhysicsSystem::class))
        baseSystems.add(provider.get(CollisionSystem::class))
    }

    fun update(deltaTime: Float) {
        baseSystems.forEach { system -> system.update(deltaTime) }
        stateService.currentState.systems.forEach { system -> system.update(deltaTime) }
    }

    fun onFrameEnd() {
        baseSystems.forEach { system -> system.onFrameEnd() }
        stateService.currentState.systems.forEach { system -> system.onFrameEnd() }
    }
}
