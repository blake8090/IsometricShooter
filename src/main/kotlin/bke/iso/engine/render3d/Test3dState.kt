package bke.iso.engine.render3d

import bke.iso.engine.Game
import bke.iso.engine.State
import bke.iso.engine.System
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.KeyBinding
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Vector3

private const val CAMERA_MOVE_SPEED = 3f

class Test3dState(override val game: Game) : State() {
    override val systems: Set<System> = emptySet()

    override suspend fun load() {
        game.assets.loadAsync("game")

        game.input.keyMouse.bind(
            "moveX",
            KeyBinding(Input.Keys.A, ButtonState.DOWN),
            KeyBinding(Input.Keys.D, ButtonState.DOWN)
        )

        game.input.keyMouse.bind(
            "moveY",
            KeyBinding(Input.Keys.S, ButtonState.DOWN),
            KeyBinding(Input.Keys.W, ButtonState.DOWN)
        )
    }

    override fun update(deltaTime: Float) {
        val delta = Vector3(
            game.input.poll("moveX") * CAMERA_MOVE_SPEED,
            game.input.poll("moveY") * CAMERA_MOVE_SPEED,
            0f
        )
        game.renderer3D.moveCamera(delta.scl(deltaTime))
    }
}