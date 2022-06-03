package bke.iso.util

import bke.iso.Singleton
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Singleton
class ReflectionService {
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> findSubTypesWithAnnotation(
        basePackage: String,
        annotation: KClass<out Annotation>,
        baseType: KClass<T>
    ): List<KClass<out T>> {
        return Reflections(basePackage, Scanners.TypesAnnotated)
            .getTypesAnnotatedWith(annotation.java)
            .map { javaClass -> javaClass.kotlin }
            .filter { type -> type.isSubclassOf(baseType) }
            .map { type -> type as KClass<T> }
    }
}
