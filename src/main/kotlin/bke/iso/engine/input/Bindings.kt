package bke.iso.engine.input

sealed class Binding

abstract class ButtonBinding : Binding() {
    abstract val code: Int
    abstract val state: ButtonState
}

enum class ButtonState {
    UP,
    DOWN,
    PRESSED,
    RELEASED
}

abstract class AxisBinding : Binding() {
    abstract val code: Int
}
