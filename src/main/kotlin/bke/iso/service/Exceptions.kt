package bke.iso.service

import kotlin.reflect.KClass

class MissingAnnotationsException(message: String) : RuntimeException(message)

class NoServiceFoundException(kClass: KClass<*>) : RuntimeException("No service found for class ${kClass.simpleName}")

class CircularDependencyException(message: String) : RuntimeException(message)

class MissingInstanceException(baseClass: KClass<*>, implClass: KClass<*>) :
    RuntimeException("Expected instance of class ${implClass.simpleName} for service ${baseClass.simpleName}")

class ServiceCreationException(message: String, e: Exception): RuntimeException(message, e)
