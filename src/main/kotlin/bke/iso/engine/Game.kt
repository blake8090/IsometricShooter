package bke.iso.engine

import bke.iso.engine.state.State
import kotlin.reflect.KClass

interface Game {
    val initialState: KClass<out State>

    fun setup()
}
