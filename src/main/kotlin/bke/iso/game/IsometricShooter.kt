package bke.iso.game

import bke.iso.engine.Engine
import bke.iso.engine.Game
import bke.iso.engine.input.ButtonState
import bke.iso.game.actor.player.system.RELOAD_ACTION
import bke.iso.game.actor.player.system.SHOOT_ACTION
import bke.iso.game.weapon.WeaponPropertiesCache
import com.badlogic.gdx.Input
import com.studiohartman.jamepad.ControllerAxis
import com.studiohartman.jamepad.ControllerButton

class IsometricShooter : Game() {

    override val windowTitle = "Isometric Shooter"

    override val folderName = "IsometricShooter"

    override fun start(engine: Engine) {
        engine.assets.addCache(WeaponPropertiesCache(engine.serializer))

        bindInput(engine)

        engine.states.setState<MainMenuState>()
    }

    private fun bindInput(engine: Engine) {
        with(engine.input.keyMouse) {
            bindKey("toggleDebug", Input.Keys.M, ButtonState.PRESSED)

            bindKey("run", Input.Keys.SHIFT_LEFT, ButtonState.DOWN)
            bindKey("jump", Input.Keys.SPACE, ButtonState.PRESSED)
            bindKey("crouch", Input.Keys.C, ButtonState.PRESSED)

            bindCompositeKey(
                action = "moveX",
                negativeKey = Input.Keys.A,
                negativeState = ButtonState.DOWN,
                positiveKey = Input.Keys.D,
                positiveState = ButtonState.DOWN,
            )

            bindCompositeKey(
                action = "moveY",
                negativeKey = Input.Keys.S,
                negativeState = ButtonState.DOWN,
                positiveKey = Input.Keys.W,
                positiveState = ButtonState.DOWN,
            )

            bindMouse(SHOOT_ACTION, Input.Buttons.LEFT, ButtonState.DOWN)
            bindKey(RELOAD_ACTION, Input.Keys.R, ButtonState.PRESSED)

            bindKey("useMedkit", Input.Keys.Q, ButtonState.PRESSED)
            bindKey("interact", Input.Keys.E, ButtonState.PRESSED)
        }

        with(engine.input.controller) {
            bindAxis("moveX", ControllerAxis.LEFTX)
            bindAxis("moveY", ControllerAxis.LEFTY, true)

            bindAxis("cursorX", ControllerAxis.RIGHTX)
            bindAxis("cursorY", ControllerAxis.RIGHTY)

            bindButton("run", ControllerButton.LEFTBUMPER, ButtonState.DOWN)
            bindButton("jump", ControllerButton.A, ButtonState.PRESSED)
            bindButton("crouch", ControllerButton.LEFTSTICK, ButtonState.PRESSED)

            bindAxis(SHOOT_ACTION, ControllerAxis.TRIGGERRIGHT)
            bindButton(RELOAD_ACTION, ControllerButton.X, ButtonState.PRESSED)

            bindButton("useMedkit", ControllerButton.DPAD_UP, ButtonState.PRESSED)
            bindButton("interact", ControllerButton.Y, ButtonState.PRESSED)
        }
    }
}
