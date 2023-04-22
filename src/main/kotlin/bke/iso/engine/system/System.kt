package bke.iso.engine.system

import bke.iso.service.TransientService

interface System : TransientService {
    fun update(deltaTime: Float)
}
