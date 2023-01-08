package bke.iso.service

class MissingAnnotationsException(message: String) : RuntimeException(message)

class NoServiceFoundException(message: String) : RuntimeException(message)

class CircularDependencyException(message: String) : RuntimeException(message)

class ServiceCreationException(message: String) : RuntimeException(message)