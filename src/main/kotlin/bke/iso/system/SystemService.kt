package bke.iso.system

import bke.iso.di.ServiceContainer
import bke.iso.di.Singleton
import kotlin.reflect.KClass

abstract class System {
    open fun init() {}
    open fun update(deltaTime: Float) {}
}

@Singleton
class SystemService(private val container: ServiceContainer) {
    private val systems = mutableSetOf<System>()

    fun registerSystems(systemClasses: Set<KClass<out System>>) {
        systemClasses.forEach { systemClass ->
            val instance = container.createInstance(systemClass)
            instance.init()
            systems.add(instance)
        }
    }

    fun update(deltaTime: Float) {
        systems.forEach { system: System -> system.update(deltaTime) }
    }
}
