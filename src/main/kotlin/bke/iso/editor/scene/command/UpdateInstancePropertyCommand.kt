package bke.iso.editor.scene.command

import bke.iso.editor.core.EditorCommand
import kotlin.reflect.KMutableProperty1

class UpdateInstancePropertyCommand<T : Any>(
    private val instance: T,
    private val property: KMutableProperty1<out T, *>,
    private val newValue: Any
) : EditorCommand() {

    override val name = "UpdateInstancePropertyCommand-${instance::class.simpleName}"

    private var previousValue: Any? = null

    override fun execute() {
        previousValue = property.getter.call(instance)
        property.setter.call(instance, newValue)
    }

    override fun undo() {
        property.setter.call(instance, previousValue)
    }
}
