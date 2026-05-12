package bke.iso.editor.core.command

import kotlin.reflect.KMutableProperty1

class UpdatePropertyCommand<T : Any>(
    private val instance: T,
    private val property: KMutableProperty1<out T, *>,
    private val newValue: Any
) : EditorCommand() {

    override val name = "UpdatePropertyCommand"

    private var previousValue: Any? = null

    override fun execute() {
        previousValue = property.getter.call(instance)
        property.setter.call(instance, newValue)
    }

    override fun undo() {
        property.setter.call(instance, previousValue)
    }
}
