package bke.iso.engine.input.source

import bke.iso.engine.input.Binding

abstract class InputSource {

    protected val buttonBindings = ButtonBindings().apply {
        isButtonDown = { binding -> checkButtonDown(binding) }
    }

    fun update() {
        buttonBindings.update()
    }

    abstract fun bind(action: String, binding: Binding)

    abstract fun checkButtonDown(binding: Binding): Boolean

    abstract fun poll(action: String): Float
}
