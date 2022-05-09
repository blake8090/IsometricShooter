package bke.iso.ioc

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import kotlin.reflect.KClass
import kotlin.reflect.cast

class MissingBindingException(message: String) : RuntimeException(message)
class MissingConstructorException(message: String) : RuntimeException(message)
class DuplicateBindingException(message: String) : RuntimeException(message)

data class Binding<T : Any>(
    val interfaceClass: KClass<T>,
    val implementationClass: KClass<out T>
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Service

// TODO: add unit tests
class IocContainer {
    private val log = LoggerFactory.getLogger(IocContainer::class.java)
    private val bindingMap = mutableMapOf<KClass<*>, Binding<*>>()

    init {
        registerService<IocContainer>()
    }

    fun registerFromClassPath(classPath: String) {
        Reflections(classPath, Scanners.TypesAnnotated)
            .getTypesAnnotatedWith(Service::class.java)
            .filterNotNull()
            .map { clazz -> clazz.kotlin }
            .forEach { clazz -> registerService(clazz) }
    }

    fun <T : Any> registerService(binding: Binding<T>) {
        if (bindingMap.containsKey(binding.interfaceClass)) {
            throw DuplicateBindingException("Class ${binding.interfaceClass.simpleName} has already been registered")
        }
        bindingMap[binding.interfaceClass] = binding
        log.debug(
            "Registered implementation '{}' for interface '{}'",
            binding.implementationClass.simpleName,
            binding.interfaceClass.simpleName
        )
    }

    fun <T : Any> registerService(clazz: KClass<T>) {
        registerService(Binding(clazz, clazz))
    }

    inline fun <reified T : Any> registerService() =
        registerService(T::class)

    fun <T : Any> getService(interfaceClass: KClass<T>): T {
        val binding = bindingMap[interfaceClass]
            ?: throw MissingBindingException("No binding found for class ${interfaceClass.simpleName}")
        return interfaceClass.cast(createInstance(binding.implementationClass))
    }

    inline fun <reified T : Any> getService(): T = getService(T::class)

    private fun <T : Any> createInstance(implementationClass: KClass<out T>): T {
        log.trace("Creating instance for implementation class {}", implementationClass.simpleName)

        val constructor = implementationClass.constructors.firstOrNull()
            ?: throw MissingConstructorException("Expected constructor for class '${implementationClass.simpleName}'")

        val dependencies = constructor.parameters.map { parameter -> parameter.type.classifier as KClass<*> }
            .map { instanceClass -> getService(instanceClass) }.toTypedArray()

        return if (dependencies.isNotEmpty()) {
            constructor.call(*dependencies)
        } else {
            constructor.call()
        }
    }
}
