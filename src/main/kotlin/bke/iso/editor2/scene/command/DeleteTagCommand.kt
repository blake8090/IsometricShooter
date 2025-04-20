package bke.iso.editor2.scene.command

import bke.iso.editor2.EditorCommand
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Tags

data class DeleteTagCommand(
    val actor: Actor,
    val tag: String
) : EditorCommand() {

    override val name: String = "DeleteTag"

    override fun execute() {
        val tags = checkNotNull(actor.get<Tags>()) {
            "Expected actor $actor to have a Tags component"
        }

        val tagsList = tags.tags.toMutableList()
        check(tagsList.remove(tag)) {
            "Expected tag $tag in tags component"
        }
        actor.add(Tags(tagsList))
    }

    override fun undo() {
        val tags = checkNotNull(actor.get<Tags>()) {
            "Expected actor $actor to have a Tags component"
        }

        val tagsList = tags.tags.toMutableList()
        check(tagsList.add(tag)) {
            "Tag $tag already added"
        }
        actor.add(Tags(tagsList))
    }
}