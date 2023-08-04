package bke.iso.old.engine.system

import bke.iso.old.service.TransientService

interface System : TransientService {
    fun update(deltaTime: Float)

    fun onFrameEnd() {}
}
