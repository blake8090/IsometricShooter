package bke.iso.editor.scene.command

import bke.iso.editor.core.EditorCommand
import bke.iso.engine.world.entity.Entity
import bke.iso.engine.world.entity.Tags

data class DeleteTagCommand(
    val entity: Entity,
    val tag: String
) : EditorCommand() {

    override val name: String = "DeleteTag"

    override fun execute() {
        val tags = checkNotNull(entity.get<Tags>()) {
            "Expected entity $entity to have a Tags component"
        }

        val tagsList = tags.tags.toMutableList()
        check(tagsList.remove(tag)) {
            "Expected tag $tag in tags component"
        }
        entity.add(Tags(tagsList))
    }

    override fun undo() {
        val tags = checkNotNull(entity.get<Tags>()) {
            "Expected entity $entity to have a Tags component"
        }

        val tagsList = tags.tags.toMutableList()
        check(tagsList.add(tag)) {
            "Tag $tag already added"
        }
        entity.add(Tags(tagsList))
    }
}