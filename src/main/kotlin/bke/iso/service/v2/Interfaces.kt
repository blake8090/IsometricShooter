package bke.iso.service.v2

sealed interface Service {
    fun create() {}
    fun dispose() {}
}

interface TransientService : Service

interface SingletonService : Service
