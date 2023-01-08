package bke.iso.v2.engine.state

import bke.iso.service.Provider
import bke.iso.service.Singleton
import kotlin.reflect.KClass

@Singleton
class StateService(private val stateProvider: Provider<State>) {

    var currentState: State = EmptyState()
        private set

    fun update(deltaTime: Float) {
        currentState.update(deltaTime)
    }

    fun setState(kClass: KClass<out State>) {
        currentState.stop()
        currentState = stateProvider.get(kClass)
        currentState.start()
    }
}
