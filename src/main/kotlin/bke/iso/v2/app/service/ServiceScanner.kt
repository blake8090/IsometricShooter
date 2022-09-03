package bke.iso.v2.app.service

import bke.iso.engine.di.Singleton
import org.reflections.Reflections

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Service

class ServiceScanner {
    fun scanClasspath(classPath: String): List<Class<*>> {
        return Reflections(classPath)
            .getTypesAnnotatedWith(Singleton::class.java)
            .filterNotNull()
    }
}
