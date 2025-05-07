package bke.iso.editor.actor.command

import bke.iso.editor.EditorCommand
import bke.iso.engine.world.actor.Component
import kotlin.reflect.KMutableProperty1

class UpdateComponentPropertyCommand(
    private val component: Component,
    private val property: KMutableProperty1<out Component, *>,
    private val newValue: Any
) : EditorCommand() {

    override val name = "UpdateMemberProperty - ${component::class.simpleName}.${property.name}"

    private var previousValue: Any? = null

    override fun execute() {
        previousValue = property.getter.call(component)
        property.setter.call(component, newValue)
    }

    override fun undo() {
        property.setter.call(component, previousValue)
    }
}
