package bke.iso.editor.entity.command

import bke.iso.editor.core.EditorCommand
import bke.iso.engine.collision.Collider
import bke.iso.engine.render.Sprite
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Component
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

data class AddComponentCommand(
    val components: MutableList<Component>,
    val referenceEntity: Entity,
    val componentType: KClass<out Component>
) : EditorCommand() {

    override val name = "AddComponent"

    private var addedComponent: Component? = null

    override fun execute() {
        val newComponent = componentType.createInstance()
        components.add(newComponent)
        addedComponent = newComponent

        if (newComponent is Sprite || newComponent is Collider) {
            referenceEntity.add(newComponent)
        }
    }

    override fun undo() {
        val component = checkNotNull(addedComponent) {
            "Expected an added component"
        }
        components.remove(component)
        referenceEntity.remove(component::class)
    }
}
