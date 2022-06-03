package bke.iso

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import kotlin.reflect.KClass
import kotlin.reflect.cast

class MissingBindingException(message: String) : RuntimeException(message)
class MissingConstructorException(message: String) : RuntimeException(message)
class DuplicateBindingException(message: String) : RuntimeException(message)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Singleton

// TODO: add unit tests
class ServiceContainer {
    private val log = LoggerFactory.getLogger(ServiceContainer::class.java)

    private val implementationByInterfaceMap = mutableMapOf<KClass<*>, KClass<*>>()
    private val instanceMap = mutableMapOf<KClass<*>, Any>()

    init {
        registerService(ServiceContainer::class)
        instanceMap[ServiceContainer::class] = this
    }

    /**
     * Searches the given [classPath] for classes annotated with [Singleton] and registers them as services.
     */
    fun registerFromClassPath(classPath: String) {
        Reflections(classPath, Scanners.TypesAnnotated)
            .getTypesAnnotatedWith(Singleton::class.java)
            .filterNotNull()
            .forEach { clazz -> registerService(clazz.kotlin) }
    }

    /**
     * Given an [implementationClass], registers a service with the interface the same as the implementation.
     */
    fun <T : Any> registerService(implementationClass: KClass<T>) =
        registerService(implementationClass, implementationClass)

    /**
     * Registers a service with the interface as [interfaceClass] and the implementation as [implementationClass].
     *
     * @throws DuplicateBindingException if the interface class has already been registered
     */
    fun <T : Any, U : T> registerService(interfaceClass: KClass<T>, implementationClass: KClass<U>) {
        if (implementationByInterfaceMap.containsKey(interfaceClass)) {
            throw DuplicateBindingException(
                "Interface class ${interfaceClass.simpleName} has already been"
                        + " registered to class ${implementationClass.simpleName}"
            )
        }
        implementationByInterfaceMap[interfaceClass] = implementationClass
        log.debug(
            "Registered implementation '{}' for interface '{}'",
            implementationClass.simpleName,
            interfaceClass.simpleName
        )
    }

    /**
     * Returns an instance of the service [T].
     *
     * @throws MissingBindingException if no service exists for class [T]
     */
    inline fun <reified T : Any> getService(): T = getService(T::class)

    /**
     * Returns an instance of a service with the given [interfaceClass].
     *
     * @throws MissingBindingException if no service exists for the [interfaceClass]
     */
    fun <T : Any> getService(interfaceClass: KClass<T>): T {
        val implementationClass = implementationByInterfaceMap[interfaceClass]
            ?: throw MissingBindingException("No binding found for class ${interfaceClass.simpleName}")
        val instance = instanceMap.getOrPut(implementationClass) { createInstance(implementationClass) }
        return interfaceClass.cast(instance)
    }

    /**
     * Creates an instance of the given [implementationClass], and injects all dependencies in the constructor.
     */
    fun <T : Any> createInstance(implementationClass: KClass<out T>): T {
        log.trace("Creating instance for implementation class '$implementationClass.simpleName'")

        val constructor = implementationClass.constructors
            .firstOrNull()
            ?: throw MissingConstructorException("Expected constructor for class '${implementationClass.simpleName}'")

        val dependencies = constructor.parameters
            .map { parameter -> parameter.type.classifier as KClass<*> }
            .map { instanceClass -> getService(instanceClass) }
            .toTypedArray()

        return constructor.call(*dependencies)
    }
}
