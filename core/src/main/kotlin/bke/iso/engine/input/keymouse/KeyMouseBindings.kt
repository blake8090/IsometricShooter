package bke.iso.engine.input.keymouse

import bke.iso.engine.input.ButtonBinding
import bke.iso.engine.input.ButtonState

data class MouseBinding(
    override val code: Int,
    override val state: ButtonState
) : ButtonBinding()

data class KeyBinding(
    override val code: Int,
    override val state: ButtonState
) : ButtonBinding()
