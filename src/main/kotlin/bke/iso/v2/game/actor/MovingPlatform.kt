package bke.iso.v2.game.actor

import bke.iso.v2.engine.world.Component

data class MovingPlatform(
    val speed: Float = 2f,
    val maxZ: Float = 2f,
    val minZ: Float = 0f,
    val pauseSeconds: Float = 1f
) : Component() {

    var state: State = State.UP

    enum class State {
        UP,
        DOWN
    }
}
