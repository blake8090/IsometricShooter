package bke.iso.service

sealed interface Service {
    fun create() {}
    fun dispose() {}
}

interface TransientService : Service

interface SingletonService : Service
