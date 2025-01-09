package bke.iso.editor.v2.actor

import bke.iso.editor.v2.core.EditorCommand
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
        property.setter.call(previousValue)
    }
}
