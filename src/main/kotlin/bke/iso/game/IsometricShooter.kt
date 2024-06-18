package bke.iso.game

import bke.iso.engine.Game
import bke.iso.engine.GameInfo
import bke.iso.engine.input.ButtonState
import bke.iso.engine.input.ControllerAxisBinding
import bke.iso.engine.input.ControllerBinding
import bke.iso.engine.input.KeyBinding
import bke.iso.engine.input.MouseBinding
import bke.iso.game.weapon.WeaponPropertiesCache
import com.badlogic.gdx.Input

class IsometricShooter : GameInfo() {

    override val windowTitle = "Isometric Shooter"

    override fun start(game: Game) {
        game.assets.addCache(WeaponPropertiesCache(game.serializer))

        bindInput(game)

        game.setState(MainMenuState::class)
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
                bke.iso.game.player.SHOOT_ACTION to MouseBinding(
                    Input.Buttons.LEFT,
                    ButtonState.DOWN
                ),
                bke.iso.game.player.RELOAD_ACTION to KeyBinding(
                    Input.Keys.R,
                    ButtonState.PRESSED
                ),
                "crouch" to KeyBinding(Input.Keys.C, ButtonState.PRESSED),
                "useMedkit" to KeyBinding(Input.Keys.Q, ButtonState.PRESSED),
                "openDoor" to KeyBinding(Input.Keys.E, ButtonState.PRESSED)
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
                    com.studiohartman.jamepad.ControllerButton.LEFTBUMPER.ordinal,
                    ButtonState.DOWN
                ),
                "moveX" to ControllerAxisBinding(com.studiohartman.jamepad.ControllerAxis.LEFTX.ordinal),
                "moveY" to ControllerAxisBinding(com.studiohartman.jamepad.ControllerAxis.LEFTY.ordinal, true),
                "cursorX" to ControllerAxisBinding(com.studiohartman.jamepad.ControllerAxis.RIGHTX.ordinal),
                "cursorY" to ControllerAxisBinding(com.studiohartman.jamepad.ControllerAxis.RIGHTY.ordinal),
                "jump" to ControllerBinding(
                    com.studiohartman.jamepad.ControllerButton.A.ordinal,
                    ButtonState.PRESSED
                ),
                bke.iso.game.player.SHOOT_ACTION to ControllerAxisBinding(com.studiohartman.jamepad.ControllerAxis.TRIGGERRIGHT.ordinal),//ControllerBinding(ControllerButton.RIGHTBUMPER.ordinal, ButtonState.DOWN),
                bke.iso.game.player.RELOAD_ACTION to ControllerBinding(
                    com.studiohartman.jamepad.ControllerButton.X.ordinal,
                    ButtonState.PRESSED
                ),
                "crouch" to ControllerBinding(
                    com.studiohartman.jamepad.ControllerButton.LEFTSTICK.ordinal,
                    ButtonState.PRESSED
                ),
                "useMedkit" to ControllerBinding(
                    com.studiohartman.jamepad.ControllerButton.DPAD_UP.ordinal,
                    ButtonState.PRESSED
                ),
                "openDoor" to ControllerBinding(
                    com.studiohartman.jamepad.ControllerButton.Y.ordinal,
                    ButtonState.PRESSED
                )
            )
        }
    }
}
