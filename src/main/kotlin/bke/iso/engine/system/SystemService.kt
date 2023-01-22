package bke.iso.engine.system

import bke.iso.service.Provider
import bke.iso.service.Singleton
import bke.iso.engine.physics.PhysicsSystem
import bke.iso.engine.state.State
import bke.iso.service.PostInit

@Singleton
class SystemService(private val systemProvider: Provider<System>) {

    private val baseSystems = mutableListOf<System>()

    @PostInit
    fun setup() {
        baseSystems.add(systemProvider.get(PhysicsSystem::class))
    }

    fun update(state: State, deltaTime: Float) {
        baseSystems.forEach { system -> system.update(deltaTime) }
        state.systems.forEach { system -> system.update(deltaTime) }
    }
}