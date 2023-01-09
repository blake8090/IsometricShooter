package bke.iso.v2.engine

import bke.iso.v2.engine.state.State
import kotlin.reflect.KClass

interface Game {
    val initialState: KClass<out State>

    fun setup()
}
