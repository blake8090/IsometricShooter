package bke.iso.util

import bke.iso.di.Singleton
import org.reflections.Reflections
import kotlin.reflect.KClass

@Singleton
class ReflectionService {
    fun findTypesWithAnnotation(basePackage: String, annotation: KClass<out Annotation>): Set<KClass<out Any>> =
        Reflections(basePackage)
            .getTypesAnnotatedWith(annotation.java)
            .map { javaClass -> javaClass.kotlin }
            .toSet()

    inline fun <reified T : Annotation> findTypesWithAnnotation(basePackage: String): Set<KClass<out Any>> =
        findTypesWithAnnotation(basePackage, T::class)

    inline fun <reified T : Any, reified U : Annotation> findSubTypesWithAnnotation(basePackage: String): Set<KClass<out T>> =
        findTypesWithAnnotation<U>(basePackage)
            .filterIsInstance<KClass<out T>>()
            .toSet()
}
