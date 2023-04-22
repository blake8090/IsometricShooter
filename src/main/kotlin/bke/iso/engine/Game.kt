package bke.iso.engine

import bke.iso.engine.state.State
import bke.iso.service.TransientService
import kotlin.reflect.KClass

interface Game : TransientService {
    val initialState: KClass<out State>

    fun setup()
}
