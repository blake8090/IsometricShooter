package bke.iso.engine

import kotlin.reflect.KClass

abstract class GameInfo(
    val windowTitle: String,
    val defaultState: KClass<out State>
)
