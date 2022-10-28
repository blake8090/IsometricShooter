package bke.iso.app.service

import org.reflections.Reflections

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Service

class ServiceScanner {
    fun scanClasspath(classPath: String): List<Class<*>> {
        return Reflections(classPath)
            .getTypesAnnotatedWith(Service::class.java)
            .filterNotNull()
    }
}
