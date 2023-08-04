package bke.iso.old.service

sealed interface Service {
    fun create() {}
    fun dispose() {}
}

interface TransientService : Service

interface SingletonService : Service
