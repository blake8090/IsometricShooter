package bke.iso.editor.v2.actor

import bke.iso.editor.v2.core.EditorCommand
import bke.iso.engine.world.actor.Component
import com.badlogic.gdx.math.Vector3
import kotlin.properties.Delegates
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

class UpdateVectorXCommand(
    private val vector: Vector3,
    private val x: Float
) : EditorCommand() {

    override val name = "UpdateVectorXCommand"

    private var previous by Delegates.notNull<Float>()

    override fun execute() {
        previous = vector.x
        vector.x = x
    }

    override fun undo() {
        vector.x = previous
    }
}

class UpdateVectorYCommand(
    private val vector: Vector3,
    private val y: Float
) : EditorCommand() {

    override val name = "UpdateVectorYCommand"

    private var previous by Delegates.notNull<Float>()

    override fun execute() {
        previous = vector.y
        vector.y = y
    }

    override fun undo() {
        vector.y = previous
    }
}

class UpdateVectorZCommand(
    private val vector: Vector3,
    private val z: Float
) : EditorCommand() {

    override val name = "UpdateVectorZCommand"

    private var previous by Delegates.notNull<Float>()

    override fun execute() {
        previous = vector.z
        vector.z = z
    }

    override fun undo() {
        vector.z = previous
    }
}
