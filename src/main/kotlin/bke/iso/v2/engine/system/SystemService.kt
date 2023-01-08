package bke.iso.v2.engine.system

import bke.iso.service.Singleton
import bke.iso.v2.engine.state.StateService

@Singleton
class SystemService(private val stateService: StateService) {

//     private val baseSystems = listOf(...)

    fun update(deltaTime: Float) {
        for (system in stateService.currentState.systems) {
            system.update(deltaTime)
        }
    }
}
