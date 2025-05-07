package bke.iso.editor.actor.command

import bke.iso.editor.EditorCommand
import bke.iso.engine.collision.Collider
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Component
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

data class AddComponentCommand(
    val components: MutableList<Component>,
    val referenceActor: Actor,
    val componentType: KClass<out Component>
) : EditorCommand() {

    override val name = "AddComponent"

    private var addedComponent: Component? = null

    override fun execute() {
        val newComponent = componentType.createInstance()
        components.add(newComponent)
        addedComponent = newComponent

        if (newComponent is Sprite || newComponent is Collider) {
            referenceActor.add(newComponent)
        }
    }

    override fun undo() {
        val component = checkNotNull(addedComponent) {
            "Expected an added component"
        }
        components.remove(component)
        referenceActor.remove(component::class)
    }
}
