package bke.iso.old.engine.system

import bke.iso.old.engine.physics.PhysicsSystem
import bke.iso.old.engine.physics.CollisionSystem
import bke.iso.old.engine.state.StateService
import bke.iso.old.service.ServiceProvider
import bke.iso.old.service.SingletonService

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
