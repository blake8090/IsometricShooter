package bke.iso.engine.world.actor

import bke.iso.engine.world.GameObject
import kotlin.reflect.KClass

fun <T : Component> GameObject.has(type: KClass<T>): Boolean =
    if (this is Actor) {
        this.components.contains(type)
    } else {
        false
    }

inline fun <reified T : Component> GameObject.has(): Boolean =
    has(T::class)
