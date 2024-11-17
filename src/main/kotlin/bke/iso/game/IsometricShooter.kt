package bke.iso.game

import bke.iso.engine.Game
import bke.iso.engine.GameInfo
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.controller.ControllerAxisBinding
import bke.iso.engine.input.controller.ControllerBinding
import bke.iso.engine.input.keymouse.KeyBinding
import bke.iso.engine.input.keymouse.MouseBinding
import bke.iso.game.actor.player.system.RELOAD_ACTION
import bke.iso.game.actor.player.system.SHOOT_ACTION
import bke.iso.game.weapon.WeaponPropertiesCache
import com.badlogic.gdx.Input
import com.studiohartman.jamepad.ControllerAxis
import com.studiohartman.jamepad.ControllerButton

class IsometricShooter : GameInfo() {

    override val windowTitle = "Isometric Shooter"

    override fun start(game: Game) {
        game.assets.addCache(WeaponPropertiesCache(game.serializer))

        bindInput(game)

        game.states.setState<MainMenuState>()
    }

    private fun bindInput(game: Game) {
        with(game.input.keyMouse) {
            bind(
                "toggleDebug" to KeyBinding(Input.Keys.M, ButtonState.PRESSED),
                "placeBouncyBall" to KeyBinding(
                    Input.Keys.Z,
                    ButtonState.PRESSED
                ),
                "run" to KeyBinding(Input.Keys.SHIFT_LEFT, ButtonState.DOWN),
                "jump" to KeyBinding(Input.Keys.SPACE, ButtonState.PRESSED),
                SHOOT_ACTION to MouseBinding(
                    Input.Buttons.LEFT,
                    ButtonState.DOWN
                ),
                RELOAD_ACTION to KeyBinding(
                    Input.Keys.R,
                    ButtonState.PRESSED
                ),
                "crouch" to KeyBinding(Input.Keys.C, ButtonState.PRESSED),
                "useMedkit" to KeyBinding(Input.Keys.Q, ButtonState.PRESSED),
                "interact" to KeyBinding(Input.Keys.E, ButtonState.PRESSED)
            )
            bind(
                "moveY",
                KeyBinding(Input.Keys.S, ButtonState.DOWN),
                KeyBinding(Input.Keys.W, ButtonState.DOWN)
            )
            bind(
                "moveX",
                KeyBinding(Input.Keys.A, ButtonState.DOWN),
                KeyBinding(Input.Keys.D, ButtonState.DOWN)
            )
        }

        with(game.input.controller) {
            bind(
                "run" to ControllerBinding(
                    ControllerButton.LEFTBUMPER.ordinal,
                    ButtonState.DOWN
                ),
                "moveX" to ControllerAxisBinding(ControllerAxis.LEFTX.ordinal),
                "moveY" to ControllerAxisBinding(ControllerAxis.LEFTY.ordinal, true),
                "cursorX" to ControllerAxisBinding(ControllerAxis.RIGHTX.ordinal),
                "cursorY" to ControllerAxisBinding(ControllerAxis.RIGHTY.ordinal),
                "jump" to ControllerBinding(
                    ControllerButton.A.ordinal,
                    ButtonState.PRESSED
                ),
                SHOOT_ACTION to ControllerAxisBinding(ControllerAxis.TRIGGERRIGHT.ordinal),//ControllerBinding(ControllerButton.RIGHTBUMPER.ordinal, ButtonState.DOWN),
                RELOAD_ACTION to ControllerBinding(
                    ControllerButton.X.ordinal,
                    ButtonState.PRESSED
                ),
                "crouch" to ControllerBinding(
                    ControllerButton.LEFTSTICK.ordinal,
                    ButtonState.PRESSED
                ),
                "useMedkit" to ControllerBinding(
                    ControllerButton.DPAD_UP.ordinal,
                    ButtonState.PRESSED
                ),
                "interact" to ControllerBinding(ControllerButton.Y.ordinal, ButtonState.PRESSED)
            )
        }
    }
}
