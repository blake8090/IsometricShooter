package bke.iso.service.v2

class InvalidDependencyException(message: String) : RuntimeException(message)

class ServiceCreationException(message: String, e: Exception) : RuntimeException(message, e)

class DuplicateServiceException(message: String) : RuntimeException(message)
