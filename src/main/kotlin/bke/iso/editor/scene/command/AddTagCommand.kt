package bke.iso.editor.scene.command

import bke.iso.editor.core.command.EditorCommand
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Tags

data class AddTagCommand(
    val entity: Entity,
    val tag: String
) : EditorCommand() {

    override val name: String = "AddTag"

    override fun execute() {
        val tags = entity.getOrAdd(Tags())

        val tagsList = tags.tags.toMutableList()
        check(tagsList.add(tag)) {
            "Duplicate tag $tag in tags component"
        }
        entity.add(Tags(tagsList))
    }

    override fun undo() {
        val tags = checkNotNull(entity.get<Tags>()) {
            "Expected entity $entity to have a Tags component"
        }

        val tagsList = tags.tags.toMutableList()
        check(tagsList.remove(tag)) {
            "Could not remove tag $tag"
        }
        entity.add(Tags(tagsList))
    }
}
