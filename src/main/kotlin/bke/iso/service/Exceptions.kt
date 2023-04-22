package bke.iso.service

import kotlin.reflect.KClass

class InvalidDependencyException(message: String) : RuntimeException(message)

class ServiceCreationException(message: String, e: Exception) : RuntimeException(message, e)

class RegisterServiceException(type: KClass<*>, e: Exception) :
    RuntimeException("Error registering service '${type.simpleName}':", e)

class DuplicateServiceException(message: String) : RuntimeException(message)

class ServiceNotFoundException(message: String) : RuntimeException(message)
