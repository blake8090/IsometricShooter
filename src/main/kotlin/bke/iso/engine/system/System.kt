package bke.iso.engine.system

import bke.iso.service.v2.TransientService

interface System : TransientService {
    fun update(deltaTime: Float)
}
