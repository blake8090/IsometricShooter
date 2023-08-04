package bke.iso.old.engine

import bke.iso.old.engine.state.State
import bke.iso.old.service.TransientService
import kotlin.reflect.KClass

interface Game : TransientService {
    val initialState: KClass<out State>

    fun setup()
}
