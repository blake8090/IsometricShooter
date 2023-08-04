package bke.iso.old.engine.state

import bke.iso.old.service.ServiceProvider
import bke.iso.old.service.SingletonService
import kotlin.reflect.KClass

class StateService(private val stateProvider: ServiceProvider<State>) : SingletonService {

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
