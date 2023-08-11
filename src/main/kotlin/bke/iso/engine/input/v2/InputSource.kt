package bke.iso.engine.input.v2

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
