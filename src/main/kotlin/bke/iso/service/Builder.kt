package bke.iso.service

import org.reflections.Reflections
import kotlin.reflect.KClass

class ContainerBuilder {
    private val classes = mutableSetOf<KClass<*>>()

    fun inPackage(packageName: String) {
        Reflections(packageName)
            .getTypesAnnotatedWith(Singleton::class.java)
            .map { javaClass -> javaClass.kotlin }
            .forEach(classes::add)

        Reflections(packageName)
            .getTypesAnnotatedWith(Transient::class.java)
            .map { javaClass -> javaClass.kotlin }
            .forEach(classes::add)
    }

    fun build() =
        ServiceContainer(classes)
}

fun container(func: ContainerBuilder.() -> Unit): ServiceContainer {
    val builder = ContainerBuilder()
    builder.apply(func)
    return builder.build()
}
