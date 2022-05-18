package bke.iso.system

import bke.iso.IocContainer
import bke.iso.Service
import kotlin.reflect.KClass

open class System {
    open fun init() {}
    open fun update(deltaTime: Float) {}
}

@Service
class SystemService(private val container: IocContainer) {
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
