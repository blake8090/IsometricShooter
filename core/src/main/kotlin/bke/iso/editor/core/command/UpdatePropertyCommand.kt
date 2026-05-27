package bke.iso.editor.core.command

import kotlin.reflect.KMutableProperty1

class UpdatePropertyCommand<T : Any>(
    private val instance: T,
    private val property: KMutableProperty1<out T, *>,
    private val newValue: Any,
    private val onActionCompleted: () -> Unit = {}
) : EditorCommand() {

    override val name = "UpdatePropertyCommand"

    private var previousValue: Any? = null

    override fun execute() {
        previousValue = property.getter.call(instance)
        property.setter.call(instance, newValue)
        onActionCompleted.invoke()
    }

    override fun undo() {
        property.setter.call(instance, previousValue)
        onActionCompleted.invoke()
    }
}
