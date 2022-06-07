package bke.iso.world.entity

open class Component

data class PositionComponent(
    var x: Float = 0f,
    var y: Float = 0f
) : Component()

data class TextureComponent(var name: String) : Component()