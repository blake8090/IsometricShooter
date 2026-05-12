package bke.iso.engine.input.controller

import bke.iso.engine.input.AxisBinding
import bke.iso.engine.input.ButtonBinding
import bke.iso.engine.input.ButtonState

data class ControllerBinding(
    override val code: Int,
    override val state: ButtonState
) : ButtonBinding()

data class ControllerAxisBinding(
    override val code: Int,
    override val invert: Boolean = false
) : AxisBinding()
