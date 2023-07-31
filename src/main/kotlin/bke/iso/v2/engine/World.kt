package bke.iso.v2.engine

import bke.iso.engine.entity.Component
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

data class Actor(val id: UUID = UUID.randomUUID()) {
    val components = mutableMapOf<KClass<out Component>, Component>()
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0f

    fun <T : Component> get(type: KClass<T>): T? =
        type.safeCast(components[type])

    inline fun <reified T : Component> get(): T? =
        get(T::class)
}

class World(game: Game) : Module(game) {
    private val actors = mutableSetOf<Actor>()

    fun add(actor: Actor) {
        actors.add(actor)
    }

    fun getActors(): Set<Actor> = actors
}
