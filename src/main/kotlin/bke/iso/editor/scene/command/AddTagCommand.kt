package bke.iso.editor.scene.command

import bke.iso.editor.EditorCommand
import bke.iso.engine.world.actor.Actor
import bke.iso.engine.world.actor.Tags

data class AddTagCommand(
    val actor: Actor,
    val tag: String
) : EditorCommand() {

    override val name: String = "AddTag"

    override fun execute() {
        val tags = actor.getOrAdd(Tags())

        val tagsList = tags.tags.toMutableList()
        check(tagsList.add(tag)) {
            "Duplicate tag $tag in tags component"
        }
        actor.add(Tags(tagsList))
    }

    override fun undo() {
        val tags = checkNotNull(actor.get<Tags>()) {
            "Expected actor $actor to have a Tags component"
        }

        val tagsList = tags.tags.toMutableList()
        check(tagsList.remove(tag)) {
            "Could not remove tag $tag"
        }
        actor.add(Tags(tagsList))
    }
}
